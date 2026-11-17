package com.fxstudio.manager;

import com.fxstudio.model.FxAnimation;
import com.fxstudio.model.FxFrame;

import java.util.List;

public class FxAnimationController {

    private FxAnimation animation;
    private boolean playing;
    private double playbackSpeed = 1.0;
    private double elapsedTime;
    private long lastUpdateNanos;

    public FxAnimationController(FxAnimation animation) {
        this.animation = animation;
    }

    public void setAnimation(FxAnimation animation) {
        this.animation = animation;
        stop();
    }

    public void play() {
        if (animation == null || getFrames().isEmpty()) return;
        if (isAtEnd()) animation.setCurrentFrameIndex(0);
        playing = true;
        lastUpdateNanos = System.nanoTime();
    }

    public void pause() {
        playing = false;
        elapsedTime = 0;
    }

    public void stop() {
        playing = false;
        elapsedTime = 0;
        lastUpdateNanos = 0;
        if (animation != null) animation.setCurrentFrameIndex(0);
    }

    public boolean update() {
        if (!playing || animation == null) return false;

        long now = System.nanoTime();
        if (lastUpdateNanos == 0) {
            lastUpdateNanos = now;
            return false;
        }

        double deltaSeconds = (now - lastUpdateNanos) / 1_000_000_000.0;
        lastUpdateNanos = now;
        elapsedTime += deltaSeconds * playbackSpeed;

        List<FxFrame> frames = getFrames();
        if (frames.isEmpty()) {
            stop();
            return false;
        }

        boolean changed = false;
        double stepSeconds = getStepDurationSeconds();
        while (elapsedTime >= stepSeconds) {
            elapsedTime -= stepSeconds;
            int nextIndex = animation.getCurrentFrameIndex() + 1;
            if (nextIndex >= frames.size()) {
                animation.setCurrentFrameIndex(0);
            } else {
                animation.setCurrentFrameIndex(nextIndex);
            }
            changed = true;
        }
        return changed;
    }

    public void nextFrame() {
        if (animation == null || getFrames().isEmpty()) return;
        int next = (animation.getCurrentFrameIndex() + 1) % getFrames().size();
        animation.setCurrentFrameIndex(next);
        elapsedTime = 0;
    }

    public void previousFrame() {
        if (animation == null || getFrames().isEmpty()) return;
        int prev = animation.getCurrentFrameIndex() - 1;
        if (prev < 0) prev = getFrames().size() - 1;
        animation.setCurrentFrameIndex(prev);
        elapsedTime = 0;
    }

    public void setPlaybackSpeed(double speed) {
        if (speed > 0 && Double.isFinite(speed)) this.playbackSpeed = speed;
    }

    public double getPlaybackSpeed() { return playbackSpeed; }
    public boolean isPlaying() { return playing; }
    public int getCurrentFrameIndex() { return animation == null ? 0 : animation.getCurrentFrameIndex(); }
    public int getTotalFrames() { return getFrames().size(); }

    private List<FxFrame> getFrames() {
        return animation == null ? List.of() : animation.getFrames();
    }

    private double getStepDurationSeconds() {
        List<FxFrame> frames = getFrames();
        if (frames.size() >= 2) {
            float ageDelta = Math.abs(frames.get(1).getAge() - frames.get(0).getAge());
            if (ageDelta > 0.001f) return ageDelta / 20.0;
        }
        return 1.0 / 30.0;
    }

    private boolean isAtEnd() {
        return animation != null && animation.getCurrentFrameIndex() >= getFrames().size() - 1;
    }
}
