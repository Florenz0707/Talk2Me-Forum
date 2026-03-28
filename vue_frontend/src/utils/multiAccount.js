import {
  ACTIVE_ACCOUNT_KEY,
  clearAuthSession,
  getActiveAccount,
  setActiveAccount,
  setAuthToken,
  setRefreshToken,
  setUserAvatar,
  setUserInfo,
} from "./authStorage";

const ACCOUNTS_KEY = "accounts";

export { ACTIVE_ACCOUNT_KEY };

export const multiAccountManager = {
  getAllAccounts() {
    const data = localStorage.getItem(ACCOUNTS_KEY);
    return data ? JSON.parse(data) : {};
  },

  getActiveUsername() {
    return getActiveAccount();
  },

  saveAccount(username, token, refreshToken, userInfo) {
    const accounts = this.getAllAccounts();
    accounts[username] = {
      token,
      refreshToken,
      userInfo,
      lastLogin: Date.now(),
    };
    localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts));
    setActiveAccount(username);
  },

  switchAccount(username) {
    const accounts = this.getAllAccounts();
    if (!accounts[username]) return false;

    setActiveAccount(username);
    const account = accounts[username];
    setAuthToken(account.token);
    setRefreshToken(account.refreshToken);
    setUserInfo(account.userInfo);
    setUserAvatar(
      account.userInfo?.avatar || account.userInfo?.avatar_url || "",
    );

    window.dispatchEvent(
      new CustomEvent("authChange", {
        detail: { isAuthenticated: true, userInfo: account.userInfo },
      }),
    );
    return true;
  },

  removeAccount(username) {
    const accounts = this.getAllAccounts();
    delete accounts[username];
    localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts));

    if (this.getActiveUsername() === username) {
      clearAuthSession();
    }
  },

  getCurrentAccount() {
    const username = this.getActiveUsername();
    if (!username) return null;
    const accounts = this.getAllAccounts();
    return accounts[username] || null;
  },

  updateCurrentToken(token, refreshToken) {
    const username = this.getActiveUsername();
    if (!username) return;

    const accounts = this.getAllAccounts();
    if (accounts[username]) {
      accounts[username].token = token;
      if (refreshToken) accounts[username].refreshToken = refreshToken;
      localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts));
    }
  },
};
