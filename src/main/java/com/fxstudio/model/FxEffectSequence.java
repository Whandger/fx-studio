package com.fxstudio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores already-rendered effects for sequential playback.
 * Each entry keeps its own captured frames and frame timing.
 */
public final class FxEffectSequence {

    public record Entry(String name, List<FxFrame> frames, double frameStepTicks) {
        public Entry {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Effect name cannot be empty.");
            }
            if (frames == null || frames.isEmpty()) {
                throw new IllegalArgumentException("An effect must contain at least one frame.");
            }
            if (!Double.isFinite(frameStepTicks) || frameStepTicks <= 0.0) {
                throw new IllegalArgumentException("Frame step must be greater than zero.");
            }
            frames = List.copyOf(frames);
        }

        public double frameDurationSeconds() {
            // Minecraft uses 20 ticks per second.
            return frameStepTicks / 20.0;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private int currentEntryIndex;
    private int currentFrameIndex;

    public void add(Entry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    public void add(String name, List<FxFrame> frames, double frameStepTicks) {
        add(new Entry(name, frames, frameStepTicks));
    }

    public void remove(int index) {
        if (index < 0 || index >= entries.size()) return;

        entries.remove(index);

        if (entries.isEmpty()) {
            restart();
            return;
        }

        if (index < currentEntryIndex) {
            currentEntryIndex--;
        } else if (index == currentEntryIndex) {
            currentEntryIndex = Math.min(currentEntryIndex, entries.size() - 1);
            currentFrameIndex = 0;
        }

        clampCurrentFrame();
    }

    public void move(int from, int to) {
        if (from < 0 || from >= entries.size() || to < 0 || to >= entries.size() || from == to) {
            return;
        }

        Entry moved = entries.remove(from);
        entries.add(to, moved);

        if (currentEntryIndex == from) {
            currentEntryIndex = to;
        } else if (from < currentEntryIndex && to >= currentEntryIndex) {
            currentEntryIndex--;
        } else if (from > currentEntryIndex && to <= currentEntryIndex) {
            currentEntryIndex++;
        }
    }

    public Entry getCurrentEntry() {
        return entries.isEmpty() ? null : entries.get(currentEntryIndex);
    }

    public FxFrame getCurrentFrame() {
        Entry entry = getCurrentEntry();
        if (entry == null || entry.frames().isEmpty()) return null;

        int safeIndex = Math.max(0, Math.min(currentFrameIndex, entry.frames().size() - 1));
        return entry.frames().get(safeIndex);
    }

    public boolean advanceFrame() {
        Entry current = getCurrentEntry();
        if (current == null) return false;

        if (currentFrameIndex + 1 < current.frames().size()) {
            currentFrameIndex++;
            return true;
        }

        if (currentEntryIndex + 1 < entries.size()) {
            currentEntryIndex++;
            currentFrameIndex = 0;
            return true;
        }

        return false;
    }

    public boolean previousFrame() {
        if (entries.isEmpty()) return false;

        if (currentFrameIndex > 0) {
            currentFrameIndex--;
            return true;
        }

        if (currentEntryIndex > 0) {
            currentEntryIndex--;
            currentFrameIndex = entries.get(currentEntryIndex).frames().size() - 1;
            return true;
        }

        return false;
    }

    public void restart() {
        currentEntryIndex = 0;
        currentFrameIndex = 0;
    }

    public void setPosition(int entryIndex, int frameIndex) {
        if (entries.isEmpty()) {
            restart();
            return;
        }

        currentEntryIndex = Math.max(0, Math.min(entryIndex, entries.size() - 1));
        currentFrameIndex = Math.max(0, frameIndex);
        clampCurrentFrame();
    }

    private void clampCurrentFrame() {
        Entry entry = getCurrentEntry();
        if (entry == null) {
            currentFrameIndex = 0;
        } else {
            currentFrameIndex = Math.min(currentFrameIndex, entry.frames().size() - 1);
        }
    }

    public int getCurrentEntryIndex() { return currentEntryIndex; }
    public int getCurrentFrameIndex() { return currentFrameIndex; }
    public int size() { return entries.size(); }
    public boolean isEmpty() { return entries.isEmpty(); }
    public List<Entry> getEntries() { return Collections.unmodifiableList(entries); }

    public void clear() {
        entries.clear();
        restart();
    }
}
