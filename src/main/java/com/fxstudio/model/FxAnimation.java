package com.fxstudio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FxAnimation {

    private final List<FxFrame> frames = new ArrayList<>();
    private int currentFrame = 0;

    public void addFrame(FxFrame frame) {
        if (frame != null) frames.add(frame);
    }

    public void addFrames(List<FxFrame> newFrames) {
        if (newFrames != null) newFrames.forEach(this::addFrame);
    }

    public FxFrame getFrame(int index) {
        if (frames.isEmpty()) return null;
        int safeIndex = Math.max(0, Math.min(index, frames.size() - 1));
        return frames.get(safeIndex);
    }

    public FxFrame getCurrentFrame() { return getFrame(currentFrame); }

    public boolean nextFrame() {
        if (currentFrame >= frames.size() - 1) return false;
        currentFrame++;
        return true;
    }

    public boolean previousFrame() {
        if (currentFrame <= 0) return false;
        currentFrame--;
        return true;
    }

    public void setCurrentFrame(int index) {
        if (frames.isEmpty()) {
            currentFrame = 0;
            return;
        }
        currentFrame = Math.max(0, Math.min(index, frames.size() - 1));
    }

    public void setCurrentFrameIndex(int index) { setCurrentFrame(index); }
    public int getCurrentFrameIndex() { return currentFrame; }
    public int getFrameCount() { return frames.size(); }
    public List<FxFrame> getFrames() { return Collections.unmodifiableList(frames); }
    public boolean isEmpty() { return frames.isEmpty(); }
    public void clear() {
        frames.clear();
        currentFrame = 0;
    }
}
