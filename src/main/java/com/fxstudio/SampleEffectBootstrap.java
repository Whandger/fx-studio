package com.fxstudio;

import com.fxstudio.manager.FxSourceManager;
import com.fxstudio.model.FxProject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public final class SampleEffectBootstrap {

    private SampleEffectBootstrap() {}

    public static void populateDefaultSamples(FxProject project) throws IOException {
        Path srcDir = project.getProjectDirectory().resolve("src");
        List<String> names = List.of(
                "PrayerVfxPrimitives.java",
                "PrayerVfxGeometry.java",
                "PrayerFireballVfx.java"
        );

        Path fireballPath = null;
        for (String name : names) {
            try (InputStream in = SampleEffectBootstrap.class.getResourceAsStream("/samples/" + name)) {
                if (in != null) {
                    String code = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    Path dest = srcDir.resolve(name);
                    FxSourceManager.saveSource(project, dest, code);
                    if (name.equals("PrayerFireballVfx.java")) {
                        fireballPath = dest;
                    }
                }
            }
        }
        if (fireballPath != null) {
            project.setMainSourceFile(fireballPath);
        }
    }
}
