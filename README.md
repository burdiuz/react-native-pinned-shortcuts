# @actualwave/react-native-pinned-shortcuts

Pin Android launcher shortcuts that open your React Native app on a deep link, optionally with a custom icon.

A pinned shortcut is an icon the user places on the home screen. Tapping it fires an `ACTION_VIEW` intent with your URL,
restricted to your own app. Typical uses: open a specific document, start a saved item or run a configured action in one tap.

Plain React Native module (no Expo dependency). Android only; on other platforms `isSupported()` is `false` and
`requestPin()` resolves `false`.

## Install

```sh
npm install @actualwave/react-native-pinned-shortcuts
```

Autolinking picks it up. Rebuild the native app (`npx react-native run-android`, or `npx expo prebuild` + a native build for Expo).
The library adds nothing to your manifest and needs no permissions.

## Make the app open from a URL (required)

The shortcut opens `url` in **your app only**, so the app must declare an intent filter for that URL's scheme. The library cannot
do this for you: the scheme is yours. Without the filter, the shortcut is created but tapping it does nothing.

**Expo:** set a `scheme` in `app.json`, then rebuild (`npx expo prebuild`):

```json
{ "expo": { "scheme": "myapp" } }
```

**Bare React Native:** add an intent filter to the main activity in `android/app/src/main/AndroidManifest.xml`:

```xml
<activity android:name=".MainActivity" android:launchMode="singleTask" ...>
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="myapp" />
  </intent-filter>
</activity>
```

`singleTask` makes a tap on a shortcut deliver the URL to the already-running app instead of starting a second copy.

Then read the URL in JS like any deep link:

```ts
import { Linking } from 'react-native';

const initial = await Linking.getInitialURL();          // app was started by the shortcut
const sub = Linking.addEventListener('url', ({ url }) => handle(url)); // app was already running
```

With Expo Router or React Navigation, configure their linking and the URL is routed for you.

## API

```ts
import { isSupported, requestPin } from '@actualwave/react-native-pinned-shortcuts';

if (isSupported()) {
  const accepted = await requestPin({
    id: 'doc-42',
    label: 'Report',
    url: 'myapp://documents/42',
    iconPath: 'file:///data/user/0/com.myapp/files/report.png', // optional
    background: '#1F2937',                                       // optional
  });
}
```

### `isSupported(): boolean`

Whether the default launcher can pin shortcuts. Call it before showing a "Add to home screen" button. It is a synchronous
native call and returns `false` on non-Android platforms or launchers without pin support.

### `requestPin(options): Promise<boolean>`

Asks the launcher to pin a shortcut.

| Option | Type | Description |
| --- | --- | --- |
| `id` | `string` | Stable id. Pinning the same id again **updates** that shortcut (label, icon, URL) instead of adding another. |
| `label` | `string` | Name under the icon, used as both the short and long label. Launchers truncate long names. |
| `url` | `string` | Deep link opened in this app. The scheme must be declared in the manifest (see above). |
| `iconPath` | `string?` | Local image (`file://` URI or absolute path; PNG, JPEG or WebP). Omit to use the app icon. |
| `background` | `string?` | Icon background colour as `#RRGGBB` (also `#AARRGGBB` or a colour name). Default `#1F2937`. Invalid values fall back to dark grey. |

Resolves `true` when the launcher **accepted** the request. The user still confirms it in a system dialog (or drags the icon),
and there is no callback for their answer, so `true` does not mean the shortcut now exists. Rejects with `ERR_PIN_SHORTCUT` if
the shortcut could not be built. An `iconPath` that cannot be read or decoded is not an error: the app icon is used instead.

### Icons

With `iconPath`, the image is scaled to fit the centre 56% of a 432×432 adaptive-icon layer and drawn over a solid
`background`, so it stays inside the area every launcher mask keeps visible. Transparent PNGs work well. Without `iconPath`,
the app's launcher icon is used.

## Notes and limits

- Shortcuts survive app restarts but are removed by the launcher if the app is uninstalled or its data is cleared.
- Updating a shortcut by re-pinning its `id` works while the shortcut exists. Pinned shortcuts cannot be listed or removed from JS.
- A launcher can refuse (`isSupported()` false, or `requestPin` resolves `false`). Offer another way to reach the same content.
- Treat the URL as untrusted input in your handler: anyone can send your app an `ACTION_VIEW` intent for your scheme, not only your shortcuts.

## License

MIT
