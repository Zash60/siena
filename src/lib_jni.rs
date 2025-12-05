use jni::JNIEnv;
use jni::objects::{JClass, JIntArray};
use jni::sys::{jboolean, jint, jbyteArray};
use crate::snes::emulator::Emulator;
use crate::snes::cartridge::{Cartridge, VideoFormat};
use crate::snes::joypad::{Joypad, JoypadEvent, Button, JOYPAD_COUNT};
use crate::frontend::android::AndroidRenderer;
use crate::snes::ppu::ppu::{SCREEN_WIDTH, SCREEN_HEIGHT};
use std::sync::{Arc, Mutex};

// Singleton Unsafe (Padrão para emuladores simples em JNI)
static mut EMULATOR: Option<Emulator<AndroidRenderer>> = None;
static mut JOYPAD_SENDERS: Option<[crossbeam_channel::Sender<JoypadEvent>; JOYPAD_COUNT]> = None;

#[no_mangle]
pub extern "system" fn Java_com_example_siena_SienaNative_init(env: JNIEnv, _c: JClass, rom: jbyteArray, ipl: jbyteArray) -> jboolean {
    android_logger::init_once(android_logger::Config::default().with_min_level(log::Level::Info));
    
    let r_vec = match env.convert_byte_array(rom) { Ok(v) => v, Err(_) => return 0 };
    let i_vec = match env.convert_byte_array(ipl) { Ok(v) => v, Err(_) => return 0 };
    
    if r_vec.is_empty() || i_vec.len() != 64 { return 0; }

    let cart = match Cartridge::load(&r_vec, None) { Ok(c) => c, Err(_) => return 0 };
    let renderer = AndroidRenderer::new(SCREEN_WIDTH, SCREEN_HEIGHT).unwrap();
    let (_, senders) = Joypad::new_channel_all();
    
    unsafe { JOYPAD_SENDERS = Some(senders); }
    if let Some(s) = unsafe { &JOYPAD_SENDERS } { let _ = s[0].send(JoypadEvent::Connect); }

    let emu_res = Emulator::new(cart, &i_vec, renderer, Some(VideoFormat::NTSC));

    match emu_res {
        Ok(mut emu) => {
            // Recaptura os senders internos se o emulador os recriou
            if let Ok(s) = emu.get_joypad_senders() { unsafe { JOYPAD_SENDERS = Some(s); } }
            unsafe { EMULATOR = Some(emu); }
            1
        }
        Err(_) => 0
    }
}

#[no_mangle]
pub extern "system" fn Java_com_example_siena_SienaNative_tickFrame(_e: JNIEnv, _c: JClass) {
    unsafe { if let Some(emu) = &mut EMULATOR {
        // Roda ciclos suficientes para um frame (~60fps)
        let mut ticks = 0;
        while ticks < 300_000 { 
            match emu.tick() { Ok(_) => ticks += 80, Err(_) => break, } 
        }
    }}
}

#[no_mangle]
pub extern "system" fn Java_com_example_siena_SienaNative_getPixels(env: JNIEnv, _c: JClass, buf: JIntArray) {
    unsafe { if let Some(emu) = &mut EMULATOR {
        if let Some(rend) = &emu.cpu.bus.ppu.renderer {
            let p = rend.get_pixels();
            let _ = env.set_int_array_region(buf, 0, p.iter().map(|&x| x as i32).collect::<Vec<i32>>().as_slice());
        }
    }}
}

#[no_mangle]
pub extern "system" fn Java_com_example_siena_SienaNative_sendInput(_e: JNIEnv, _c: JClass, id: jint, down: jboolean) {
    let b = match id { 0=>Button::A, 1=>Button::B, 2=>Button::X, 3=>Button::Y, 4=>Button::Start, 5=>Button::Select, 6=>Button::Up, 7=>Button::Down, 8=>Button::Left, 9=>Button::Right, 10=>Button::L, 11=>Button::R, _=>return };
    let evt = if down == 1 { JoypadEvent::Down(b) } else { JoypadEvent::Up(b) };
    unsafe { if let Some(s) = &JOYPAD_SENDERS { let _ = s[0].send(evt); } }
}
