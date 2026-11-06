# AssetStudio AOV Android — starter project

This is the first Android-native milestone for the supplied AssetStudio AOV Windows package.

## Current functionality
- Android Storage Access Framework file picker (no broad storage permission).
- Reads a selected file's display name, size, and first 64 bytes.
- Recognizes common UnityFS, UnityRaw, UnityWeb and ZIP signatures.

## Not implemented yet
- AOV-specific decryption or bundle unwrapping.
- Unity serialized-file parsing and asset enumeration.
- Texture decoding/export.
- Mesh, skeleton, animation and FBX/OBJ export.

The original ZIP targets `net9.0` + `Microsoft.WindowsDesktop.App` and `win-x64`; it also includes Windows-only APIs and native DLLs. Those components cannot simply be copied into an Android APK. This starter is an Android-native shell, not a port of the original extraction engine.

## Build with GitHub Actions
1. Create a GitHub repository and upload these project files to its `main` branch.
2. Open the repository's **Actions** tab.
3. Run **Build Android APK** (or push to `main`).
4. Download the `AssetStudio-AOV-Android-debug` artifact from the completed workflow.
5. Extract the artifact and install `app-debug.apk` on the tablet.

Package: `com.shiro.assetstudioaov`  
Minimum Android: 8.0 (API 26)  
Target SDK: 35
