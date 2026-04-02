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

function pickFirstDefined(...values) {
  for (const value of values) {
    if (value !== undefined && value !== null && value !== "") {
      return value;
    }
  }
  return "";
}

function normalizeLegacyReplyRecord(reply) {
  return normalizeNotification({
    ...reply,
    type: "REPLY",
    rawType: "REPLY",
    eventType: getNotificationEventType(reply) || "CREATED",
    actorId: pickFirstDefined(reply?.actorId, reply?.userId),
    postId: pickFirstDefined(reply?.postId, reply?.sourcePostId),
    replyContent: pickFirstDefined(reply?.replyContent, reply?.content),
    commentContent: pickFirstDefined(
      reply?.commentContent,
      reply?.targetContent,
      reply?.originalContent,
      reply?.parentContent,
      reply?.quotedContent,
    ),
    targetId: pickFirstDefined(
      reply?.targetId,
      reply?.replyToId,
      reply?.parentId,
    ),
    targetType: pickFirstDefined(reply?.targetType, reply?.entityType),
    createTime: reply?.createTime || new Date().toISOString(),
  });
}

export function normalizeNotification(notification) {
  const rawType = getNotificationRawType(notification);
  const type = getTrackedType(rawType);
  const senderId = pickFirstDefined(
    notification?.userId,
    notification?.senderId,
    notification?.sender_id,
    notification?.actorId,
    notification?.actor_id,
    notification?.user_id,
  );
  const replyContent = pickFirstDefined(
    notification?.replyContent,
    notification?.reply?.content,
    notification?.content,
  );
  const commentContent = pickFirstDefined(
    notification?.commentContent,
    notification?.targetContent,
    notification?.originalContent,
    notification?.parentContent,
    notification?.quotedContent,
    notification?.target?.content,
    notification?.sourceContent,
  );
  const senderName = pickFirstDefined(
    notification?.senderName,
    notification?.actorName,
    notification?.actor?.name,
    notification?.actor?.username,
    notification?.sender?.name,
    notification?.sender?.username,
    notification?.username,
    notification?.user?.username,
  );
  const senderAvatar = pickFirstDefined(
    notification?.senderAvatar,
    notification?.actorAvatar,
    notification?.actor?.avatar,
    notification?.sender?.avatar,
    notification?.avatar,
    notification?.avatarUrl,
    notification?.user?.avatar,
    notification?.user?.avatarUrl,
  );
  const targetTitle = pickFirstDefined(
    notification?.targetTitle,
    notification?.postTitle,
    notification?.threadTitle,
    notification?.topicTitle,
    notification?.target?.title,
    notification?.post?.title,
    notification?.thread?.title,
    notification?.topic?.title,
  );
  const targetPreview = pickFirstDefined(
    notification?.targetPreview,
    notification?.preview,
    notification?.targetSummary,
    notification?.targetExcerpt,
    notification?.target?.preview,
    notification?.target?.summary,
    notification?.postPreview,
    notification?.threadPreview,
    replyContent,
    commentContent,
  );
  const targetUrl = pickFirstDefined(
    notification?.targetUrl,
    notification?.url,
    notification?.link,
    notification?.target?.url,
    notification?.target?.link,
  );
  const actionText = pickFirstDefined(
    notification?.actionText,
    notification?.verb,
    notification?.action,
  );
  const targetUserId = pickFirstDefined(
    notification?.targetUserId,
    notification?.target?.userId,
    notification?.target?.id,
    notification?.profileUserId,
  );

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
    userId: senderId,
    senderId,
    senderName,
    senderAvatar,
    replyContent,
    commentContent,
    targetTitle,
    targetPreview,
    targetUrl,
    actionText,
    targetUserId,
    targetId: pickFirstDefined(
      notification?.targetId,
      notification?.replyToId,
      notification?.parentId,
      notification?.target?.id,
      targetUserId,
    ),
    targetType: pickFirstDefined(
      notification?.targetType,
      notification?.entityType,
      notification?.target?.type,
    ),
    postId: pickFirstDefined(
      notification?.postId,
      notification?.post_id,
      notification?.threadId,
      notification?.thread_id,
      notification?.topicId,
      notification?.topic_id,
      notification?.sourcePostId,
      notification?.source_post_id,
      notification?.post?.id,
      notification?.post?.postId,
      notification?.thread?.id,
      notification?.thread?.postId,
      notification?.target?.postId,
    ),
  };
}

export function extractIncomingNotifications(notification) {
  if (notification?.data?.records && Array.isArray(notification.data.records)) {
    return notification.data.records.map(normalizeLegacyReplyRecord);
  }

  return [normalizeNotification(notification)];
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
  const normalizedNotifications = extractIncomingNotifications(notification);
  const trackedNotifications = normalizedNotifications.filter(
    (normalized) =>
      TRACKED_TYPES.includes(normalized.type) && normalized.id != null,
  );

  trackedNotifications.forEach((normalized) => {
    if (normalized.eventType === "DELETED") {
      notificationMeta.delete(normalized.id);
      return;
    }
    notificationMeta.set(normalized.id, normalized);
  });

  setSummaryCounts(
    buildCountsFromNotifications(Array.from(notificationMeta.values())),
  );
  return normalizedNotifications[0] || null;
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
