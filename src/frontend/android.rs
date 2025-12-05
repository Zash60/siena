use super::{new_displaybuffer, DisplayBuffer, Renderer};
use anyhow::Result;
use std::sync::{Arc, Mutex};
use std::sync::atomic::Ordering;

pub struct AndroidRenderer {
    pub display_buffer: DisplayBuffer,
    pub pixel_data: Arc<Mutex<Vec<u32>>>,
    width: usize,
    height: usize,
}

impl AndroidRenderer {
    pub fn get_pixels(&self) -> Vec<u32> {
        self.pixel_data.lock().unwrap().clone()
    }
}

impl Renderer for AndroidRenderer {
    fn new(w: usize, h: usize) -> Result<Self> {
        Ok(Self {
            display_buffer: new_displaybuffer(w, h),
            pixel_data: Arc::new(Mutex::new(vec![0; w * h])),
            width: w, height: h,
        })
    }

    fn update(&mut self) -> Result<()> {
        let mut pixels = self.pixel_data.lock().unwrap();
        for (i, chunk) in self.display_buffer.chunks_exact(4).enumerate() {
            if i < pixels.len() {
                let r = chunk[0].load(Ordering::Relaxed) as u32;
                let g = chunk[1].load(Ordering::Relaxed) as u32;
                let b = chunk[2].load(Ordering::Relaxed) as u32;
                pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
        Ok(())
    }

    fn get_buffer(&mut self) -> DisplayBuffer {
        Arc::clone(&self.display_buffer)
    }
}
