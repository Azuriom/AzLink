package com.azuriom.azlink.bukkit.injector;

import com.azuriom.azlink.common.AzLinkPlugin;
import io.netty.util.Version;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Since Spigot 1.19, netty-codec-http is no longer included, so we need to manually load it.
 * <p>
 * We are not including it in the plugin.yml 'libraries' list, because it's not needed for all Minecraft versions,
 * and the Netty version changes between Minecraft versions.
 */
public class NettyLibraryLoader {

    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar";

    private final AzLinkPlugin plugin;
    private final Path libsFolder;

    public NettyLibraryLoader(AzLinkPlugin plugin) {
        this.plugin = plugin;
        this.libsFolder = plugin.getPlatform().getDataDirectory().resolve("libs");
    }

    public void loadRequiredLibraries() throws Exception {
        try {
            Class.forName("io.netty.handler.codec.http.HttpServerCodec");
            // netty-codec-http is already loaded, all good
            return;
        } catch (ClassNotFoundException e) {
            // manually load netty-codec-http below
        }

        this.plugin.getLogger().info("Loading netty-codec-http...");

        loadLibrary("io.netty", "netty-codec-http", identifyNettyVersion());

        this.plugin.getLogger().info("Loaded netty-codec-http successfully.");
    }

    private void loadLibrary(String groupId, String artifactId, String version) throws Exception {
        Path jar = this.libsFolder.resolve(artifactId + "-" + version + ".jar");

        if (!Files.exists(jar)) {
            Files.createDirectory(jar.getParent());

            this.plugin.getLogger().warn("Downloading " + artifactId + " v" + version + "...");

            String url = String.format(MAVEN_CENTRAL, groupId.replace('.', '/'), artifactId, version, artifactId, version);

            try (InputStream in = URI.create(url).toURL().openStream()) {
                Files.copy(in, jar);
            }

            this.plugin.getLogger().info("Successfully downloaded " + artifactId + ".");
        }

        URL[] urls = {jar.toUri().toURL()};
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        Field classLoaderField = classLoader.getClass().getDeclaredField("libraryLoader");

        classLoaderField.setAccessible(true);
        classLoaderField.set(classLoader, new URLClassLoader(urls, classLoader.getParent()));
    }

    private String identifyNettyVersion() {
        Version version = Version.identify().get("netty-codec");

        return version != null ? version.artifactVersion() : "4.1.97.Final";
    }
}
