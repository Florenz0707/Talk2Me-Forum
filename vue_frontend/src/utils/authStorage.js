const TOKEN_KEY = "auth_token";
const REFRESH_TOKEN_KEY = "refresh_token";
const USER_INFO_KEY = "user_info";
const ACTIVE_ACCOUNT_KEY = "active_account";
const USER_AVATAR_KEY = "userAvatar";

const SESSION_SCOPED_KEYS = [
  TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_INFO_KEY,
  ACTIVE_ACCOUNT_KEY,
  USER_AVATAR_KEY,
];

function getSessionStorage() {
  return typeof window !== "undefined" ? window.sessionStorage : null;
}

function getLocalStorage() {
  return typeof window !== "undefined" ? window.localStorage : null;
}

function getSessionScopedItem(key) {
  const sessionStorageRef = getSessionStorage();
  const localStorageRef = getLocalStorage();

  const sessionValue = sessionStorageRef?.getItem(key);
  if (sessionValue !== null && sessionValue !== undefined) {
    return sessionValue;
  }

  const legacyValue = localStorageRef?.getItem(key);
  if (legacyValue !== null && legacyValue !== undefined && sessionStorageRef) {
    sessionStorageRef.setItem(key, legacyValue);
    localStorageRef?.removeItem(key);
  }
  return legacyValue;
}

function setSessionScopedItem(key, value) {
  const sessionStorageRef = getSessionStorage();
  const localStorageRef = getLocalStorage();

  if (!sessionStorageRef) {
    return;
  }

  if (value === null || value === undefined || value === "") {
    sessionStorageRef.removeItem(key);
  } else {
    sessionStorageRef.setItem(key, value);
  }

  if (SESSION_SCOPED_KEYS.includes(key)) {
    localStorageRef?.removeItem(key);
  }
}

function removeSessionScopedItem(key) {
  getSessionStorage()?.removeItem(key);
  getLocalStorage()?.removeItem(key);
}

function emitUserAvatarChange(avatar) {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(
    new CustomEvent("userAvatarChange", {
      detail: { avatar: avatar || "" },
    }),
  );
}

export {
  TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_INFO_KEY,
  ACTIVE_ACCOUNT_KEY,
  USER_AVATAR_KEY,
};

export function getAuthToken() {
  return getSessionScopedItem(TOKEN_KEY);
}

export function setAuthToken(token) {
  setSessionScopedItem(TOKEN_KEY, token);
}

export function getRefreshToken() {
  return getSessionScopedItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token) {
  setSessionScopedItem(REFRESH_TOKEN_KEY, token);
}

export function getUserInfo() {
  const raw = getSessionScopedItem(USER_INFO_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    removeSessionScopedItem(USER_INFO_KEY);
    return null;
  }
}

export function setUserInfo(userInfo) {
  if (!userInfo) {
    removeSessionScopedItem(USER_INFO_KEY);
    return;
  }

  setSessionScopedItem(USER_INFO_KEY, JSON.stringify(userInfo));
}

export function clearUserInfo() {
  removeSessionScopedItem(USER_INFO_KEY);
}

export function getActiveAccount() {
  return getSessionScopedItem(ACTIVE_ACCOUNT_KEY);
}

export function setActiveAccount(username) {
  setSessionScopedItem(ACTIVE_ACCOUNT_KEY, username);
}

export function clearActiveAccount() {
  removeSessionScopedItem(ACTIVE_ACCOUNT_KEY);
}

export function getUserAvatar() {
  return getSessionScopedItem(USER_AVATAR_KEY);
}

export function setUserAvatar(avatar) {
  setSessionScopedItem(USER_AVATAR_KEY, avatar || "");
  emitUserAvatarChange(avatar || "");
}

export function clearUserAvatar() {
  removeSessionScopedItem(USER_AVATAR_KEY);
  emitUserAvatarChange("");
}

export function clearLegacyAuthStorage() {
  const localStorageRef = getLocalStorage();
  SESSION_SCOPED_KEYS.forEach((key) => {
    localStorageRef?.removeItem(key);
  });
}

export function clearAuthSession() {
  SESSION_SCOPED_KEYS.forEach((key) => {
    getSessionStorage()?.removeItem(key);
  });
  clearLegacyAuthStorage();
  emitUserAvatarChange("");
}
