const USER_PROFILE_UPDATED_EVENT = "userProfileUpdated";

function normalizeUserId(userId) {
  if (userId === null || userId === undefined || userId === "") {
    return "";
  }

  return String(userId);
}

export function isSameUserId(left, right) {
  const normalizedLeft = normalizeUserId(left);
  const normalizedRight = normalizeUserId(right);

  return Boolean(normalizedLeft) && normalizedLeft === normalizedRight;
}

export function emitUserProfileUpdated(detail = {}) {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(
    new CustomEvent(USER_PROFILE_UPDATED_EVENT, {
      detail: {
        ...detail,
        userId: normalizeUserId(detail.userId),
      },
    }),
  );
}

export function onUserProfileUpdated(callback) {
  if (typeof window === "undefined" || typeof callback !== "function") {
    return () => {};
  }

  const handler = (event) => {
    callback(event.detail || {});
  };

  window.addEventListener(USER_PROFILE_UPDATED_EVENT, handler);

  return () => {
    window.removeEventListener(USER_PROFILE_UPDATED_EVENT, handler);
  };
}
