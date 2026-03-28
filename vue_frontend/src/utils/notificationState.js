import { reactive } from "vue";
import { notificationApi } from "./api";

const TRACKED_TYPES = ["LIKE", "REPLY", "FOLLOW"];
const PAGE_SIZE = 100;
const MAX_PAGES = 20;

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
    if (TRACKED_TYPES.includes(normalized.type)) {
      notificationMeta.set(normalized.id, normalized);
    }
  });
}

function buildCountsFromNotifications(notifications) {
  const counts = createEmptyCounts();

  notifications.map(normalizeNotification).forEach((notification) => {
    if (!TRACKED_TYPES.includes(notification.type) || notification.isRead) {
      return;
    }

    counts.byType[notification.type] += 1;
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

export function getNotificationEventType(notification) {
  return String(notification?.eventType || "CREATED").toUpperCase();
}

export function normalizeNotification(notification) {
  const rawType = getNotificationRawType(notification);
  const type = getTrackedType(rawType);

  return {
    ...notification,
    rawType,
    type,
    eventType: getNotificationEventType(notification),
    isRead: Boolean(notification?.isRead ?? notification?.read ?? false),
    createTime:
      notification?.createTime ||
      notification?.createdAt ||
      notification?.createdTime ||
      new Date().toISOString(),
  };
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
      setSummaryCounts(
        buildCountsFromNotifications(Array.from(notificationMeta.values())),
      );
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

  if (normalized.eventType === "DELETED") {
    notificationMeta.delete(normalized.id);
  } else {
    notificationMeta.set(normalized.id, normalized);
  }

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
