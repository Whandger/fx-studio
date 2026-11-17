package com.fxstudio.manager;

import com.fxstudio.model.FxEffectSequence;
import com.fxstudio.model.FxFrame;

/**
 * Plays a queue of captured effects without recompiling them during playback.
 */
public final class FxSequenceController {

    private FxEffectSequence sequence;
    private boolean playing;
    private boolean loopSequence;
    private double playbackSpeed = 1.0;
    private double elapsedSeconds;
    private long lastUpdateNanos;

    public FxSequenceController(FxEffectSequence sequence) {
        this.sequence = sequence;
    }

    public void setSequence(FxEffectSequence sequence) {
        this.sequence = sequence;
        stop();
    }

    public void play() {
        if (sequence == null || sequence.isEmpty()) return;

        if (isAtEnd()) {
            sequence.restart();
        }

        playing = true;
        elapsedSeconds = 0.0;
        lastUpdateNanos = System.nanoTime();
    }

    public void pause() {
        playing = false;
        elapsedSeconds = 0.0;
        lastUpdateNanos = 0L;
    }

    public void stop() {
        pause();
        if (sequence != null) sequence.restart();
    }

    /**
     * @return true when the displayed frame changed.
     */
    public boolean update() {
        if (!playing || sequence == null || sequence.isEmpty()) return false;

        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return false;
        }

        elapsedSeconds += ((now - lastUpdateNanos) / 1_000_000_000.0) * playbackSpeed;
        lastUpdateNanos = now;

        boolean changed = false;
        int safetyCounter = 0;

        while (safetyCounter++ < 1000) {
            FxEffectSequence.Entry entry = sequence.getCurrentEntry();
            if (entry == null) {
                playing = false;
                return changed;
            }

            double frameDuration = entry.frameDurationSeconds();
            if (elapsedSeconds < frameDuration) break;

            elapsedSeconds -= frameDuration;

            if (sequence.advanceFrame()) {
                changed = true;
                continue;
            }

            if (loopSequence) {
                sequence.restart();
                changed = true;
            } else {
                // Keep the final frame visible after playback ends.
                playing = false;
                elapsedSeconds = 0.0;
                changed = true;
                break;
            }
        }

        return changed;
    }

    public void nextFrame() {
        if (sequence == null || sequence.isEmpty()) return;

        if (!sequence.advanceFrame() && loopSequence) {
            sequence.restart();
        }
        elapsedSeconds = 0.0;
    }

    public void previousFrame() {
        if (sequence == null || sequence.isEmpty()) return;
        sequence.previousFrame();
        elapsedSeconds = 0.0;
    }

    public void setLoopSequence(boolean loop) {
        loopSequence = loop;
    }

    public boolean isLoopSequence() { return loopSequence; }

    public void setPlaybackSpeed(double speed) {
        if (Double.isFinite(speed) && speed > 0.0) {
            playbackSpeed = speed;
        }
    }

    public double getPlaybackSpeed() { return playbackSpeed; }
    public boolean isPlaying() { return playing; }

    private boolean isAtEnd() {
        if (sequence == null || sequence.isEmpty()) return true;

        int lastEntryIndex = sequence.size() - 1;
        FxEffectSequence.Entry lastEntry = sequence.getEntries().get(lastEntryIndex);

        return sequence.getCurrentEntryIndex() == lastEntryIndex
                && sequence.getCurrentFrameIndex() == lastEntry.frames().size() - 1;
    }
}
