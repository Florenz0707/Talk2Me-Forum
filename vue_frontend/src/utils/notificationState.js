import { reactive } from "vue";
import { notificationApi } from "./api";

const TRACKED_TYPES = ["LIKE", "REPLY", "FOLLOW"];
const PAGE_SIZE = 100;
const MAX_PAGES = 20;
const ACTOR_ID_FIELDS = [
  "senderId",
  "fromUserId",
  "sourceUserId",
  "operatorId",
  "triggerUserId",
  "actorId",
  "userId",
];
const ACTOR_NAME_FIELDS = [
  "senderName",
  "senderUsername",
  "fromUsername",
  "sourceUsername",
  "operatorName",
  "actorName",
  "username",
  "nickname",
];
const TARGET_ID_FIELDS = [
  "targetId",
  "postId",
  "replyId",
  "resourceId",
  "objectId",
  "entityId",
  "subjectId",
];
const TARGET_TYPE_FIELDS = [
  "targetType",
  "resourceType",
  "objectType",
  "entityType",
  "subjectType",
];

export const notificationSummary = reactive({
  total: 0,
  byType: {
    LIKE: 0,
    REPLY: 0,
    FOLLOW: 0,
  },
  initialized: false,
  loading: false,
});

const notificationMeta = new Map();
let refreshPromise = null;

function createEmptyCounts() {
  return {
    total: 0,
    byType: {
      LIKE: 0,
      REPLY: 0,
      FOLLOW: 0,
    },
  };
}

function getTrackedType(rawType) {
  if (rawType.includes("FOLLOW")) return "FOLLOW";
  if (rawType.includes("REPLY")) return "REPLY";
  if (rawType.includes("LIKE")) return "LIKE";
  return "";
}

function toComparableString(value) {
  if (value == null) {
    return "";
  }
  return String(value).trim().toLowerCase();
}

function collectComparableValues(notification, fields) {
  const values = new Set();

  fields.forEach((field) => {
    const value = notification?.[field];
    if (Array.isArray(value)) {
      value.forEach((item) => {
        const normalized = toComparableString(item);
        if (normalized) {
          values.add(normalized);
        }
      });
      return;
    }

    const normalized = toComparableString(value);
    if (normalized) {
      values.add(normalized);
    }
  });

  return values;
}

function setsIntersect(left, right) {
  if (!left.size || !right.size) {
    return false;
  }

  for (const value of left) {
    if (right.has(value)) {
      return true;
    }
  }

  return false;
}

function normalizeContentForMatch(content) {
  return String(content || "")
    .toLowerCase()
    .replace(
      /取消了对|取消对|取消了|取消|移除了|移除|撤回了|撤回|赞了|点赞了|点赞|获赞|收到的赞|收到赞|like|unlike/gi,
      "",
    )
    .replace(/[^\p{L}\p{N}]+/gu, "");
}

function isSimilarContentKey(left, right) {
  if (!left || !right) {
    return false;
  }

  if (left === right) {
    return true;
  }

  const minLength = Math.min(left.length, right.length);
  if (minLength < 6) {
    return false;
  }

  return left.includes(right) || right.includes(left);
}

export function getNotificationRawType(notification) {
  return String(
    notification?.rawType ||
      notification?.type ||
      notification?.notificationType ||
      "",
  ).toUpperCase();
}

export function getNotificationCategory(notification) {
  return getTrackedType(getNotificationRawType(notification));
}

export function isLikeRemovalNotification(notification) {
  const rawType = getNotificationRawType(notification);
  return (
    rawType.includes("UNLIKE") ||
    rawType.includes("CANCEL_LIKE") ||
    rawType.includes("LIKE_CANCEL") ||
    rawType.includes("LIKE_REMOVE") ||
    rawType.includes("LIKE_REMOVED")
  );
}

export function getLikeNotificationDelta(notification) {
  if (getNotificationCategory(notification) !== "LIKE") {
    return 0;
  }
  return isLikeRemovalNotification(notification) ? -1 : 1;
}

export function normalizeNotification(notification) {
  const rawType = getNotificationRawType(notification);
  const type = getTrackedType(rawType);

  return {
    ...notification,
    rawType,
    type,
    isRead: Boolean(notification?.isRead ?? notification?.read ?? false),
    createTime:
      notification?.createTime ||
      notification?.createdAt ||
      notification?.createdTime ||
      new Date().toISOString(),
  };
}

function sortNotificationsByTime(notifications) {
  return [...notifications].sort((left, right) => {
    const leftTime = new Date(left?.createTime || 0).getTime();
    const rightTime = new Date(right?.createTime || 0).getTime();

    if (Number.isNaN(leftTime) || Number.isNaN(rightTime)) {
      return 0;
    }

    return rightTime - leftTime;
  });
}

function isMatchingLikeNotification(likeNotification, removalNotification) {
  if (!likeNotification || !removalNotification) {
    return false;
  }

  if (
    likeNotification.id != null &&
    removalNotification.id != null &&
    String(likeNotification.id) === String(removalNotification.id)
  ) {
    return true;
  }

  const likeTargetIds = collectComparableValues(
    likeNotification,
    TARGET_ID_FIELDS,
  );
  const removalTargetIds = collectComparableValues(
    removalNotification,
    TARGET_ID_FIELDS,
  );
  const likeTargetTypes = collectComparableValues(
    likeNotification,
    TARGET_TYPE_FIELDS,
  );
  const removalTargetTypes = collectComparableValues(
    removalNotification,
    TARGET_TYPE_FIELDS,
  );
  const likeActorIds = collectComparableValues(
    likeNotification,
    ACTOR_ID_FIELDS,
  );
  const removalActorIds = collectComparableValues(
    removalNotification,
    ACTOR_ID_FIELDS,
  );
  const likeActorNames = collectComparableValues(
    likeNotification,
    ACTOR_NAME_FIELDS,
  );
  const removalActorNames = collectComparableValues(
    removalNotification,
    ACTOR_NAME_FIELDS,
  );

  const hasTargetIdMatch = setsIntersect(likeTargetIds, removalTargetIds);
  const hasActorIdMatch = setsIntersect(likeActorIds, removalActorIds);
  const hasActorNameMatch = setsIntersect(likeActorNames, removalActorNames);
  const hasTargetTypeMatch =
    !likeTargetTypes.size ||
    !removalTargetTypes.size ||
    setsIntersect(likeTargetTypes, removalTargetTypes);

  if (
    hasTargetIdMatch &&
    hasTargetTypeMatch &&
    (hasActorIdMatch || hasActorNameMatch)
  ) {
    return true;
  }

  const likeContentKey = normalizeContentForMatch(likeNotification?.content);
  const removalContentKey = normalizeContentForMatch(
    removalNotification?.content,
  );

  return isSimilarContentKey(likeContentKey, removalContentKey);
}

export function buildEffectiveLikeNotifications(notifications = []) {
  const normalizedNotifications = sortNotificationsByTime(
    notifications
      .map(normalizeNotification)
      .filter((item) => item.type === "LIKE"),
  );
  const effectiveLikes = [];
  const pendingRemovals = [];
  const seenLikeIds = new Set();

  normalizedNotifications.forEach((notification) => {
    if (isLikeRemovalNotification(notification)) {
      const existingIndex = effectiveLikes.findIndex((item) =>
        isMatchingLikeNotification(item, notification),
      );

      if (existingIndex !== -1) {
        effectiveLikes.splice(existingIndex, 1);
        return;
      }

      pendingRemovals.push(notification);
      return;
    }

    if (notification.id != null && seenLikeIds.has(String(notification.id))) {
      return;
    }

    const removalIndex = pendingRemovals.findIndex((item) =>
      isMatchingLikeNotification(notification, item),
    );

    if (removalIndex !== -1) {
      pendingRemovals.splice(removalIndex, 1);
      return;
    }

    if (notification.id != null) {
      seenLikeIds.add(String(notification.id));
    }
    effectiveLikes.push(notification);
  });

  return effectiveLikes;
}

function setSummaryCounts(counts) {
  notificationSummary.total = counts.total;
  TRACKED_TYPES.forEach((type) => {
    notificationSummary.byType[type] = counts.byType[type];
  });
  notificationSummary.initialized = true;
}

function syncMetaFromNotifications(notifications) {
  notificationMeta.clear();

  notifications.forEach((notification) => {
    if (notification?.id == null) {
      return;
    }
    const normalized = normalizeNotification(notification);
    notificationMeta.set(normalized.id, normalized);
  });
}

function buildCountsFromNotifications(notifications) {
  const counts = createEmptyCounts();
  const normalizedNotifications = notifications.map(normalizeNotification);
  const effectiveLikeNotifications = buildEffectiveLikeNotifications(
    normalizedNotifications,
  );

  counts.byType.LIKE = effectiveLikeNotifications.filter(
    (notification) => !notification.isRead,
  ).length;
  counts.total += counts.byType.LIKE;

  normalizedNotifications.forEach((notification) => {
    const normalized = normalizeNotification(notification);
    if (
      !TRACKED_TYPES.includes(normalized.type) ||
      normalized.isRead ||
      normalized.type === "LIKE"
    ) {
      return;
    }
    counts.byType[normalized.type] += 1;
    counts.total += 1;
  });

  return counts;
}

async function fetchAllNotifications() {
  const notifications = [];
  let page = 1;
  let totalPages = 1;

  while (page <= totalPages && page <= MAX_PAGES) {
    const res = await notificationApi.getNotifications({
      page,
      size: PAGE_SIZE,
    });
    const pageData = res?.data || {};
    const records = Array.isArray(pageData.records) ? pageData.records : [];

    notifications.push(...records);

    if (typeof pageData.pages === "number" && pageData.pages > 0) {
      totalPages = pageData.pages;
    } else if (typeof pageData.total === "number") {
      totalPages = Math.max(1, Math.ceil(pageData.total / PAGE_SIZE));
    } else if (records.length < PAGE_SIZE) {
      totalPages = page;
    } else {
      totalPages = page + 1;
    }

    if (records.length === 0) {
      break;
    }

    page += 1;
  }

  return notifications;
}

export async function refreshNotificationSummary() {
  if (refreshPromise) {
    return refreshPromise;
  }

  notificationSummary.loading = true;

  refreshPromise = (async () => {
    try {
      const notifications = await fetchAllNotifications();
      syncMetaFromNotifications(notifications);
      setSummaryCounts(buildCountsFromNotifications(notifications));
    } finally {
      notificationSummary.loading = false;
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

export function resetNotificationSummary() {
  notificationMeta.clear();
  setSummaryCounts(createEmptyCounts());
}

export function applyIncomingNotification(notification) {
  const normalized = normalizeNotification(notification);

  if (!TRACKED_TYPES.includes(normalized.type) || normalized.id == null) {
    return normalized;
  }

  notificationMeta.set(normalized.id, normalized);
  setSummaryCounts(
    buildCountsFromNotifications(Array.from(notificationMeta.values())),
  );

  return normalized;
}

export function markNotificationAsReadInSummary(notification) {
  const normalized = normalizeNotification(notification);
  if (!TRACKED_TYPES.includes(normalized.type) || normalized.id == null) {
    return;
  }

  const previous = notificationMeta.get(normalized.id);
  notificationMeta.set(normalized.id, {
    ...(previous || normalized),
    ...normalized,
    isRead: true,
  });
  setSummaryCounts(
    buildCountsFromNotifications(Array.from(notificationMeta.values())),
  );
}

export function markNotificationsAsReadInSummary(notifications = []) {
  notifications.forEach((notification) => {
    markNotificationAsReadInSummary(notification);
  });
}
