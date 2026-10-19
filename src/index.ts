/**
 * @actualwave/react-native-pinned-shortcuts — pin launcher shortcuts that open the app on a deep
 * link. Android only; on other platforms `isSupported()` is false and `requestPin()` resolves false.
 */
import { NativeModules, Platform } from 'react-native';

interface PinnedShortcutsNative {
  isSupported(): boolean;
  requestPin(
    id: string,
    label: string,
    url: string,
    iconPath: string | null,
    background: string | null,
  ): Promise<boolean>;
}

const native: PinnedShortcutsNative | undefined =
  Platform.OS === 'android' ? NativeModules.ActualwavePinnedShortcuts : undefined;

export interface PinShortcutOptions {
  /** Stable id; pinning the same id again updates that shortcut. */
  id: string;
  label: string;
  /** Deep link opened in this app, e.g. `myapp://run?file=a.jsx` (the app must handle it). */
  url: string;
  /** Local image (file:// URI or path) centred on `background`; the app icon when omitted. */
  iconPath?: string;
  /** Icon background colour, `#RRGGBB`; default dark grey. */
  background?: string;
}

/** Whether the current launcher can pin shortcuts. */
export function isSupported(): boolean {
  return native?.isSupported() ?? false;
}

/** Resolves true when the launcher accepted the request (the user still has to confirm it). */
export function requestPin(options: PinShortcutOptions): Promise<boolean> {
  if (!native) return Promise.resolve(false);
  const { id, label, url, iconPath, background } = options;
  return native.requestPin(id, label, url, iconPath ?? null, background ?? null);
}
