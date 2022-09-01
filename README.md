# 1.16.1-Extreme-Difficulty

**Plugin trailer on Iseburg's channel: https://www.youtube.com/watch?v=7Kl9jtCHIGg**

**Spigot servers, version 1.16.1**

**Java version 14 (see below for setup guide)**<sup><sub><sub><sup>dont question it i also dont know why i used this java version</sup></sub></sub></sup>

To reduce the volume of explosion noises without affecting everything else, use the "/explosionvolume set [decimal between 0 and 1]" command (you can also use "/explosionvolume get" to check the current volume).

WARNING: plugin is pretty heavyweight, do not run on a server with low RAM allocation

# Installation instructions:

1. Click on "Releases" near the right side of the screen
2. Download the jar file from the release you want under the "Assets" tab (unlike with modded clients, you do not need to download the source code zip files; those only provide the code for the mod and not the runnable file itself)
3. Put the jar file into your plugin folder in your server folder
4. Run your server
5. Join the server in creative mode because every player after the first one that joins gets debuffed for 15 seconds to prevent relog exploits

# Java 14 setup guide:

Follow this guide if the plugin doesn't seem to be doing anything and the server log has an error about mismatched/unsupported Java versions

1. Go to [AdoptOpenJDK's releases page](https://adoptopenjdk.net/releases.html) and select OpenJDK 14 Hotspot
2. Scroll down. Windows/Mac operating systems have 4 download options while Linux has 2. Find your operating system and download the first option which should be labeled as JDK
3. Click on the downloaded file to install the JDK (if you are using Linux, the installation is a bit more complicated but. You're using Linux. You should know what you're doing.)
4. At a certain step during the installation, you will see an option on the left side of the window for "set JAVA_HOME variable". Click the icon next to it and select "local drive only" (wording not exact)
    * If you don't see this, take note of which folder the JDK is being installed into, finish the installation and search up a tutorial online for changing Java versions for your operating system
5. Finish the installation process and you should now be running Java 14

# Finally done

This is a plugin made with Spigot NMS and Bukkit API.