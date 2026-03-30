import { multiAccountManager } from "./multiAccount";
import {
  clearAuthSession,
  clearUserInfo as clearStoredUserInfo,
  getAuthToken,
  getRefreshToken,
  getUserInfo as getStoredUserInfo,
  setAuthToken,
  setRefreshToken,
  setUserAvatar,
  setUserInfo,
} from "./authStorage";

const API_BASE_URL = "/talk2me/api/v1";

function decodeJwtPayload(token) {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

function pickUserId(source) {
  if (!source || typeof source !== "object") {
    return null;
  }

  return source.userId || source.id || source.uid || null;
}

function resolveUserInfoFromResponse(response) {
  if (!response || typeof response !== "object") {
    return null;
  }

  if (response.data?.user && typeof response.data.user === "object") {
    return response.data.user;
  }

  if (response.user && typeof response.user === "object") {
    return response.user;
  }

  if (response.data && typeof response.data === "object") {
    return response.data;
  }

  return null;
}

function storeResolvedUserInfo(userInfo, context) {
  const resolvedUserId = pickUserId(userInfo);

  console.log(`[Talk2Me][${context}] userInfo`, {
    id: resolvedUserId,
    userInfo,
  });

  if (!userInfo || typeof userInfo !== "object") {
    return null;
  }

  setUserInfo(userInfo);
  setUserAvatar(userInfo.avatar || userInfo.avatar_url || "");
  return userInfo;
}

function maskToken(token) {
  if (!token) {
    return "";
  }

  if (token.length <= 16) {
    return token;
  }

  return `${token.slice(0, 12)}...${token.slice(-4)}`;
}

export class ApiError extends Error {
  constructor(message, statusCode = null, errorCode = null) {
    super(message);
    this.name = "ApiError";
    this.statusCode = statusCode;
    this.errorCode = errorCode;
  }
}

function isTokenUsable(token, payload) {
  if (!token || !payload) {
    return false;
  }

  if (payload.exp && payload.exp * 1000 < Date.now()) {
    console.warn("Token expired, falling back to cookie session");
    return false;
  }

  return true;
}

async function parseResponseData(response) {
  try {
    return await response.json();
  } catch {
    return {
      message: response.statusText,
    };
  }
}

async function performRequest(url, options) {
  const response = await fetch(url, options);
  const responseData = await parseResponseData(response);
  return { response, responseData };
}

async function fetchAndStoreCurrentUserInfo(context) {
  const response = await request("/users/profile", "GET", null, true);
  const userInfo = resolveUserInfoFromResponse(response);

  if (!userInfo) {
    console.warn(`[Talk2Me][${context}] failed to resolve userInfo`, response);
    return null;
  }

  return storeResolvedUserInfo(userInfo, context);
}

function emitUnauthenticated() {
  clearAuthSession();
  window.dispatchEvent(
    new CustomEvent("authChange", { detail: { isAuthenticated: false } }),
  );
  window.dispatchEvent(new CustomEvent("open-login-modal"));
}

async function request(endpoint, method, data = null, useAuth = true) {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = {
    "Content-Type": "application/json",
  };

  const authMode =
    useAuth === "optional" ? "optional" : useAuth ? "required" : "none";
  const token = getAuthToken();
  const payload = token ? decodeJwtPayload(token) : null;
  const shouldAttachBearer =
    authMode !== "none" && isTokenUsable(token, payload);

  if (shouldAttachBearer) {
    headers.Authorization = `Bearer ${token}`;
  }

  const buildOptions = (requestHeaders) => ({
    method,
    headers: requestHeaders,
    credentials: "include",
  });

  const options = buildOptions(headers);

  if (data) {
    options.body = JSON.stringify(data);
  }

  try {
    let { response, responseData } = await performRequest(url, options);

    if (response.status === 401 && shouldAttachBearer) {
      const cookieOnlyHeaders = {
        ...headers,
      };
      delete cookieOnlyHeaders.Authorization;

      const retryOptions = buildOptions(cookieOnlyHeaders);
      if (data) {
        retryOptions.body = JSON.stringify(data);
      }

      const retryResult = await performRequest(url, retryOptions);
      if (retryResult.response.ok) {
        return retryResult.responseData;
      }

      response = retryResult.response;
      responseData = retryResult.responseData;
    }

    if (!response.ok) {
      let errorMessage =
        responseData.message || `Request failed: ${response.status}`;

      switch (response.status) {
        case 401:
          errorMessage =
            responseData.message || "Unauthorized or session expired";
          if (authMode !== "none") {
            emitUnauthenticated();
          }
          break;
        case 403:
          errorMessage = responseData.message || "Forbidden";
          break;
        case 404:
          errorMessage =
            responseData.message || "Requested resource was not found";
          break;
        case 500:
          errorMessage = responseData.message || "Internal server error";
          break;
      }

      throw new ApiError(errorMessage, response.status, responseData.errorCode);
    }

    return responseData;
  } catch (error) {
    if (error.name !== "ApiError") {
      throw new ApiError(
        "Network error, please check your connection",
        null,
        "NETWORK_ERROR",
      );
    }
    throw error;
  }
}
export const authApi = {
  // 1. 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫梺鍛婄懄濞叉﹢濡甸崟顖氱疀闁靛鍨规禍楣冩煣韫囷絽浜濇繛鍫熸煥閳规垶骞婇柛濠冾殕閹便劑骞橀钘夊壒婵炴挻鍩冮崑鎾绘煙椤栨瑧鍔嶇€垫澘瀚伴獮鍥敇閻愭彃顥?- POST /api/v1/auth/verification
  // 闂傚倸鍊烽悞锕€顪冮崹顕呯劷闁秆勵殔缁€澶愭倵閿濆骸澧插┑顔挎珪閵囧嫰骞掗崱妞惧闂備礁纾划顖毼涢崘鈺€绻嗛柟闂寸鍞梺闈涱檧闂勫嫬顭块幋锔解拻濞撴埃鍋撴繛浣冲吘娑㈠籍閸喎娈戦柣鐘荤細閵嗏偓闁哄绉归弻鏇熷緞閸繂惟婵炲瓨绮岀紞濠囧蓟閿熺姴鐐婇柕濠傛处瀹€绋跨暦閹达箑绠婚悗娑櫭鎾绘⒑閼姐倕鏋涢柛瀣工铻為柣鏂垮悑閳锋垿鏌熼懖鈺佷粶闁告柣鍊濋弻娑氣偓锝庡亞濞叉挳鏌熼娆戠獢妤犵偛顑夐弫鍌炴寠婢跺﹤顥樻繝鐢靛仩閹活亞绱為埀顒佺箾閸滃啰绉鐐寸墬缁轰粙宕ㄦ繝鍕箞闂備焦瀵у褰掑磿閹惰棄绀夐悗锝庡亞缁?
  async getVerification() {
    try {
      const response = await request("/auth/verification", "POST", null, false);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旀儳甯ュ┑鐐茬墢閺咁偊鍩€椤掆偓閸樻粓宕戦幘缁樼厱闁规澘鍚€缁ㄥ吋銇勯銏⑿ф慨濠傛惈鐓ら悹鍥у级閻忓棛绱撴担鎻掍壕婵炴挻鍩冮崑鎾斥攽閿涘嫭鏆鐐差儔閹晠宕樺顔荤穿?",
        error,
      );
      throw error;
    }
  },

  // 2. 闂傚倸鍊烽悞锕€顪冮崹顕呯劷闁秆勵殔缁€澶屸偓骞垮劚椤︻垶寮伴妷锔剧闁瑰鍋熼。鏌ユ倵濮橆剦鐓奸柡灞诲姂瀵潙螖閳ь剚绂嶆ィ鍐╁仭婵犲﹤瀚欢鏌ユ煟韫囨梻绠為柛鈹垮灲瀵噣宕煎顏佹櫊閺屾洘寰勯崼婵嗗闂?- POST /api/v1/auth/register
  async register(registrationData) {
    try {
      return await request("/auth/register", "POST", registrationData, false);
    } catch (error) {
      console.error(
        "婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐烘⒑閸忛棿鑸柛搴や含濞嗐垽鎮欓崫鍕獓闂佸壊鍋掗崑鍕椤栨粍鍋?",
        error,
      );
      throw error;
    }
  },

  // 3. 闂傚倸鍊风粈渚€骞夐敍鍕殰闁跨喓濮寸紒鈺呮⒑椤掆偓缁夋挳鎷戦悢灏佹斀闁绘ê寮舵径鍕煕鐎ｃ劌濮傞柡灞炬礃缁绘繆绠涢弴鐘虫闂備胶绮幐璇裁洪悢鑲烘盯宕ㄩ幖顓熸櫌婵犮垼娉涢敃锝囪姳閻㈠憡鈷戦柛婵勫劚娴滃ジ鏌涙繝鍐畵闁伙絽鍢查埢搴ㄥ箼閸愨晜娅嶉梻浣瑰缁嬫垹鈧凹鍙冮敐?- POST /api/v1/auth/refresh
  async refreshToken() {
    try {
      const refreshToken = getRefreshToken();

      if (!refreshToken) {
        throw new ApiError("Missing refresh token", null, "NO_REFRESH_TOKEN");
      }

      const response = await request(
        "/auth/refresh",
        "POST",
        { refresh_token: refreshToken },
        false,
      );

      // 闂傚倸鍊风粈渚€骞栭鈷氭椽濡舵径瀣槐闂侀潧艌閺呮盯鎷戦悢灏佹斀闁绘ɑ褰冮弳鐔搞亜閳哄啫鍘撮柡灞炬礋瀹曠厧鈹戦崶銊﹀€锋俊?- 闂傚倸鍊风粈渚€骞栭銈囩煋闁绘垶鏋荤紞鏍ь熆鐠虹尨鍔熼柡鍡愬€曢妴鎺戭潩閿濆懍澹曢柣搴ゎ潐濞插繘宕曢弻銉ョ畾闁哄啫鐗婇弲鏌ュ箹鐎涙绠樼痪鍙ョ矙濮婃椽宕崟鍨㈤梺鍝勬噺缁挸顕ｆ繝姘╅柕澶婂暞濡炶棄顕ｆ禒瀣垫晣闁绘柨鍢叉竟鍕⒒娴ｅ憡鍟炴繛璇х畵瀹曟粌顫濋鑺ョ亖闂佸壊鍋呭ú姗€鎮″▎鎾村仯闁搞儻绲洪崑鎾绘惞椤愩倓澹曠紓鍌氬€风欢锟犲窗閹捐绀夐柟瀛樼箥閸?
      if (response.data && response.data.access_token) {
        setAuthToken(response.data.access_token);
        multiAccountManager.updateCurrentToken(
          response.data.access_token,
          response.data.refresh_token,
        );
      } else if (response.access_token) {
        setAuthToken(response.access_token);
        multiAccountManager.updateCurrentToken(
          response.access_token,
          response.refresh_token,
        );
      }

      if (response.data && response.data.refresh_token) {
        setRefreshToken(response.data.refresh_token);
      } else if (response.refresh_token) {
        setRefreshToken(response.refresh_token);
      }

      return response;
    } catch (error) {
      console.error(
        "濠电姷鏁搁崑娑㈩敋椤撶喐鍙忓ù鍏兼綑绾捐淇婇妶鍕妽婵炲懐濮电换婵嬫濞戝崬鍓扮紓浣插亾闁糕剝绋掗埛鎴︽煕椤垵娅橀柛搴㈠姍閺岋綁骞橀姘濠电姷鏁告慨浼村垂濞差亜纾块柤娴嬫櫅閸ㄦ繈鏌涢幘妤€瀚弸?",
        error,
      );
      this.logout();
      throw error;
    }
  },

  // 4. 闂傚倸鍊烽悞锕€顪冮崹顕呯劷闁秆勵殔缁€澶屸偓骞垮劚椤︻垶寮伴妷锔剧闁瑰瓨鐟ラ悘顏堟煕鎼达紕鐒搁柟顔肩秺瀹曞爼顢旈崟顓燁嚄缂傚倷绀侀ˇ顖氼焽閿熺姴钃熼柣鏃傚帶缁犳帡鏌熼悜妯虹仴濠殿喖閰ｉ幃?- POST /api/v1/auth/login
  async login(username, password) {
    try {
      const response = await request(
        "/auth/login",
        "POST",
        { username, password },
        false,
      );

      console.log(
        "闂傚倸鍊峰ù鍥儍椤愶箑骞㈤柍杞扮劍椤斿嫮绱撻崒姘偓鍝ョ矓鐎靛摜鐭撻柣鐔稿閺嗭附銇勯幇鈺佺労闁告艾顑夐弻娑樷槈閸楃偟浠╅梺?",
        response,
      );
      let token, refreshToken, userInfo;

      if (response.data) {
        token = response.data.access_token;
        refreshToken = response.data.refresh_token;
        userInfo = response.data.user;
      } else {
        token = response.access_token;
        refreshToken = response.refresh_token;
        userInfo = response.user || { username: response.username };
      }

      if (token) {
        setAuthToken(token);
        console.log(
          "Token闂備浇顕уù鐑藉箠閹捐绠熼梽鍥Φ閹版澘绀冩い鏃囨娴犻亶姊洪棃娴ュ牓寮插☉姘棜?",
          token.substring(0, 20) + "...",
        );
      }
      if (refreshToken) {
        setRefreshToken(refreshToken);
      }

      if (userInfo) {
        userInfo = storeResolvedUserInfo(userInfo, "login response");
      }

      if (token && !pickUserId(userInfo)) {
        userInfo =
          (await fetchAndStoreCurrentUserInfo("login profile sync")) ||
          userInfo;
      }

      if (userInfo) {
        multiAccountManager.saveAccount(
          username,
          token,
          refreshToken,
          userInfo,
        );
      }

      this._emitAuthChangeEvent(true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊峰ù鍥儍椤愶箑骞㈤柍杞扮劍椤斿嫮绱撻崒姘偓鍝ョ矓鐎靛摜鐭撻梻鍫熶緱閸ゆ鏌涢弴銊ュ缂傚秴娲﹂妵鍕箛閳轰礁澹?",
        error,
      );
      throw error;
    }
  },

  // 5. 闂傚倸鍊峰ù鍥儍椤愶箑骞㈤柍杞扮劍椤斿嫰姊绘担瑙勫仩闁稿鍊濆畷锝夊礃椤斿吋鐎悗骞垮劚椤︻垶鏌嬮崶銊х瘈濠电姴鍊归崳鑺ャ亜閺冣偓濡啫顫忛悜妯侯嚤婵炲棙鍨硅摫闂備浇宕甸崰鍡涘礉瀹ュ拋鍤楅柛鏇ㄥ灠缁€瀣亜閺嶃劍鐨戦柣鎾村灴濮婃椽鎮欓鍐棟闂佺锕ˉ鎾诲箲閵忋倕骞㈡繛鎴炵懅閸橀潧顪冮妶鍡樺瘷闁告劦浜欑槐娆戠磽?
  logout() {
    clearAuthSession();
    this._emitAuthChangeEvent(false);
  },

  // 闂傚倸鍊风粈渚€骞夐敍鍕殰闁圭儤鍤氬ú顏呮櫇闁逞屽墴閹箖鎮滈懞銉ユ濡炪倖甯婄粈渚€顢旈幖浣光拺闁圭瀛╃壕鐢告煕鐎ｎ偅灏甸柟?
  switchAccount(username) {
    return multiAccountManager.switchAccount(username);
  },

  // 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╃紓浣哄О閸庣敻寮诲鍫闂佸憡鎸鹃崰搴敋閿濆鍨傛い鎰╁灮缁愮偞绻濋悽闈浶㈤柛濠勵焾閳绘捇濡烽埡鍌楁嫼闁荤姴顑呮绋课涢銏犲惞婵炲棙鍨圭壕鐣屸偓骞垮劚閹虫劘鍊撮梻浣烘嚀閸㈣尙绱炴繝鍥ф瀬闁告劦鍠栭悞鍨亜閹烘垵鏆斿ù?
  getAllAccounts() {
    return multiAccountManager.getAllAccounts();
  },

  // 闂傚倷娴囬褍霉閻戣棄绠犻柟鍓х帛閸ゆ劖銇勯弽顐粶闁活厽顨婇悡顐﹀炊閵婏妇顦梺娲诲幗閹告悂鍩為幋锔藉亹鐎规洖娴傞弳锟犳⒑缂佹绠橀柛鐘崇墵楠炲啳銇愰幒鎴犵杸闂佸憡鎸烽懗鍫曞汲閻樼粯鍋?
  removeAccount(username) {
    multiAccountManager.removeAccount(username);
  },

  // 婵犵數濮烽。钘壩ｉ崨鏉戠；闁逞屽墴閺屾稓鈧綆鍋呭畷宀勬煛瀹€瀣？濞寸媴濡囬幏鐘诲箵閹烘埈娼ュ┑鐘殿暯閳ь剙鍟跨痪褔鏌熼鐓庘偓鎼佹偩閻戣棄唯闁冲搫鍊瑰▍鍥⒑闁偛鑻晶瀵糕偓瑙勬礃閻熲晠寮幘缁樺亹闁告劖褰冩竟鍐⒑閼姐倕孝婵炴祴鏅濇禍绋库枎閹寸姷鐓?
  isAuthenticated() {
    const token = getAuthToken();
    return isTokenUsable(token, token ? decodeJwtPayload(token) : null);
  },

  async ensureSession() {
    const token = getAuthToken();
    const payload = token ? decodeJwtPayload(token) : null;
    const hasUsableToken = isTokenUsable(token, payload);
    const storedUserInfo = getStoredUserInfo();
    const storedUserId = pickUserId(storedUserInfo);

    if (hasUsableToken && storedUserId) {
      console.log("[Talk2Me][ensureSession] existing stored userInfo", {
        id: storedUserId,
        userInfo: storedUserInfo,
      });
      return true;
    }

    try {
      const syncContext = hasUsableToken
        ? "ensureSession profile sync"
        : "ensureSession cookie session sync";
      await fetchAndStoreCurrentUserInfo(syncContext);

      this._emitAuthChangeEvent(true);
      return true;
    } catch (error) {
      if (error.statusCode === 401) {
        clearAuthSession();
        this._emitAuthChangeEvent(false);
        return false;
      }
      throw error;
    }
  },

  // 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旇崵鏆楁繛瀛樼矊缂嶅﹪寮婚悢鐓庣畾鐟滃秹寮虫潏銊ｄ簻闁靛牆鎳忛崳褰掓婢舵劖鐓熸慨妤€妫楅弸娑㈡煃瑜滈崜姘躲€冮崱娑樼柧?
  getToken() {
    return getAuthToken();
  },

  // 濠?JWT token 濠电姷鏁搁崑鐐哄垂閸洖绠归柍鍝勬噹閻鏌嶈閸撴盯骞冮灏栨瀻闁规儳顕崢钘夘渻閵堝棙鈷愭俊鍙夊浮瀹曟鐣濋崟顒傚幐婵炶揪缍佸褔鍩€椤掍胶绠為柣娑卞枤閳ь剨缍嗘禍鐐哄汲鐎ｎ喗鐓熸俊銈傚亾闁绘妫濋崺鈧い鎺嶇閳绘洟鏌＄仦璇插闁宠棄顦灒闁煎鍊楅鈧琽ken 闂傚倸鍊烽悞锕傛儑瑜版帒鍨傞柣鐔稿閺嗭附銇勯幒鎴濐仼缂佺姵鐗楁穱濠囧Χ閸涱厽娈堕梺鍛婎殕绾板秹濡甸崟顖氬唨闁靛ě鍛幘闂備礁鎲″Λ蹇涘闯閿濆钃熸繛鎴炃氶弸搴ㄦ煙闁箑澧伴柛妯诲姉缁辨挻鎷呮禒瀣懙闁诲孩鑹鹃妴姊?闂傚倷娴囬褏鈧稈鏅濈划娆撳箳濡炲皷鍋撻崘顔煎耿婵炴垼椴搁弲鈺呮煟韫囨洖浠滃褌绮欓崺?username闂?
  getUsernameFromToken() {
    const token = getAuthToken();
    if (!token) return null;
    const payload = decodeJwtPayload(token);
    return payload ? payload.sub : null;
  },

  // 濠?JWT token 濠电姷鏁搁崑鐐哄垂閸洖绠归柍鍝勬噹閻鏌嶈閸撴盯骞冮灏栨瀻闁规儳顕崢钘夘渻閵堝棙鈷愭俊鍙夊浮瀹曟鐣濋崟顒傚幐婵炶揪缍佸褔鍩€椤掍胶绠為柣娑卞枤閳ь剨缍嗛崜娑㈡儗濞嗘挻鐓?
  getUserIdFromToken() {
    const token = getAuthToken();
    const payload = token ? decodeJwtPayload(token) : null;
    const userInfo = getStoredUserInfo();

    return pickUserId(payload) || pickUserId(userInfo);
  },

  getCurrentUserId() {
    const userInfo = getStoredUserInfo();
    const storedUserId = pickUserId(userInfo);
    const token = getAuthToken();
    const payload = token ? decodeJwtPayload(token) : null;
    const payloadUserId = pickUserId(payload);
    const resolvedUserId = storedUserId || payloadUserId || null;

    console.debug("[Talk2Me][getCurrentUserId]", {
      storedUserInfo: userInfo,
      storedUserId,
      payload,
      payloadUserId,
      resolvedUserId,
    });

    if (storedUserId) {
      return storedUserId;
    }

    return payloadUserId;
  },

  getTokenPayload() {
    const token = getAuthToken();
    return token ? decodeJwtPayload(token) : null;
  },

  getAuthDebugInfo() {
    const token = getAuthToken();
    const payload = token ? decodeJwtPayload(token) : null;
    const userInfo = getStoredUserInfo();
    const tokenUserId = pickUserId(payload);
    const storedUserId = pickUserId(userInfo);

    return {
      hasToken: Boolean(token),
      tokenPreview: maskToken(token),
      payload,
      payloadUserId: tokenUserId,
      storedUserInfo: userInfo,
      storedUserId,
      resolvedUserId: tokenUserId || storedUserId || null,
    };
  },

  debugAuth(label = "auth") {
    const snapshot = this.getAuthDebugInfo();

    console.group(`[Talk2Me][${label}] Auth Debug`);
    console.log("resolvedUserId:", snapshot.resolvedUserId);
    console.log("payloadUserId:", snapshot.payloadUserId);
    console.log("storedUserId:", snapshot.storedUserId);
    console.log("tokenPreview:", snapshot.tokenPreview);
    console.log("payload:", snapshot.payload);
    console.log("storedUserInfo:", snapshot.storedUserInfo);
    console.groupEnd();

    return snapshot;
  },

  // 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫梺绋款儍閸旀垿寮婚弴鐔虹瘈闊洦娲滈弳鐘绘⒑缂佹ɑ灏版繛鍙夌墳瑜颁線姊洪幖鐐插姷缂佽尪濮ら弲鍫曞箵閹?
  getUserInfo() {
    const userInfo = getStoredUserInfo();

    console.log("[Talk2Me][getUserInfo] stored userInfo", {
      id: pickUserId(userInfo),
      userInfo,
    });

    return userInfo;
  },

  // 闂傚倸鍊风粈渚€骞栭鈷氭椽濡舵径瀣槐闂侀潧艌閺呮盯鎷戦悢灏佹斀闁绘ê寮舵径鍕煕鐎ｃ劌濮傞柡灞炬礃缁绘繆绠涢弴鐘虫闂備胶绮幐璇裁洪弽顒€绲归梻浣规偠閸庤崵寰婇懞銉︽珷闁圭粯宕?
  updateUserInfo(userInfo) {
    setUserInfo(userInfo);
  },

  // 婵犵數濮烽弫鎼佸磻閻愬搫绠伴柟闂寸缁犵姵淇婇婵勨偓鈧柡瀣Ч楠炴牕菐椤掆偓婵′粙鏌涚€ｃ劌濮傞柡灞炬礃缁绘繆绠涢弴鐘虫闂備胶绮幐璇裁洪弽顒€绲归梻浣规偠閸庤崵寰婇懞銉︽珷闁圭粯宕?
  clearUserInfo() {
    clearStoredUserInfo();
  },

  // 闂傚倷娴囧畷鐢稿窗閹扮増鍋￠弶鍫氭櫇娑撳秹鏌熸潏鍓хシ濞存粌缍婇弻娑氫沪閸撗呯厑婵犳鍨伴妶鎼佸蓟閺囥垹閱囨繝闈涚墕閳崬鈹戦埄鍐ㄧ祷闁绘鎹囧濠氭晲閸涘倻鍠栧畷褰掝敊閹冪細闂傚倷绶氶埀顒傚仜閼活垶宕㈤崫銉х＜闁逞屽墯閹峰懏鎱ㄩ幇顏呯潖濠电姰鍨奸崺鏍礉閺囩姷鐭嗙€光偓閸曨剛鍘搁悗骞垮劚缁绘劙銆呴鍕厽闁靛绠戦悘銉╂?
  _emitAuthChangeEvent(isAuthenticated) {
    const event = new CustomEvent("authChange", {
      detail: {
        isAuthenticated,
        userInfo: isAuthenticated ? this.getUserInfo() : null,
      },
    });
    window.dispatchEvent(event);
  },

  // 闂傚倸鍊烽懗鍫曞磿閻㈢鐤炬繝濠傜墕閸ㄥ倿鏌ｉ姀銏╃劸缂佺姵鑹鹃…璺ㄦ崉閻戞ɑ鎷辨繝娈垮灠閵堟悂寮婚弴銏犻唶婵犻潧鐗嗛埅鍗炩攽閳╁啫绲婚柣妤佹崌瀵鏁愰崨鍌滃枛瀹曞綊顢欓幆褍缂氶梻鍌欑窔閳ь剛鍋涢懟顖炲储閸濄儳纾奸柍褜鍓氶幏鍛叏閹邦亝鐫忓┑鐘灱閸╂牠宕濋弴鐘电焼?
  onAuthChange(callback) {
    window.addEventListener("authChange", (event) => {
      callback(event.detail.isAuthenticated, event.detail.userInfo);
    });
  },
};

// ==================== 濠电姷鏁搁崑娑㈡偤閵娧冨灊鐎光偓閳ь剟骞冮鈧、鏇㈡晝閳ь剟鎮為崹顐犱簻闁瑰瓨绻勬禒銏ゆ煟韫囨柨鏁猻t-controller (闂傚倷鐒﹂惇褰掑春閸曨垰鍨傞梺顒€绉甸崑瀣煛閸ャ儱鐏柛瀣ㄥ€栫换娑㈠幢濡櫣浠兼繛鎴炴尭缁夊綊寮婚妸銉㈡婵☆垯璀︽禒楣冩⒑缁嬪尅鍔熼柛瀣ㄥ€濆濠氭偄閻戞ê鐨版繝銏ｆ硾閿曘倝寮冲▎鎾寸厽闁圭偓娼欓悘鎾煛? ====================
export const postApi = {
  // 1. 闂傚倸鍊风粈渚€骞栭銈嗗仏妞ゆ劧绠戠壕鍧楁煙缂併垹娅橀柡浣割儐娣囧﹪濡堕崨顔兼闂佹悶鍊栧濠氬焵椤掑倹鍤€閻庢凹鍘奸…鍨熼悡搴ｇ瓘闂佺鍕垫畷闁绘挻娲樻穱濠囧Χ閸曨厼濡界紓浣插亾闁告劏鏅濈粻鍓х棯閺夊灝鑸归柣蹇ョ秮閺屸剝绗熼崶褎鐝濋悗娈垮枛閳ь剛鍣ュΣ楣冩⒑绾懏顥夌紒澶婄秺瀵鏁愭径濞⑩晠鏌曟径鍫濆姶濞寸厧鍊搁—鍐Χ閸℃顦ㄧ紓浣割儐閹告儳危閹版澘绠虫俊銈傚亾妤犵偑鍨介弻锟犲炊閵婏妇娈ょ紓浣哄У瀹€绋款潖?- GET /api/v1/posts/{id}
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: PostDO }
  async getPostById(id) {
    try {
      const response = await request(`/posts/${id}`, "GET", null, "optional");
      // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗗姉閸犲孩绂嶆ィ鍐╃厽闁绘柨鎼。鍏肩節閳ь剚瀵肩€涙鍘搁梺绯曟閸樺墽绮旈鈧弻鐔碱敍濮橆剚娈婚悗瑙勬礈閸犳牠銆侀弴銏℃櫜闁糕剝顨呮俊濂告⒒娴ｅ憡璐￠柛妯犲洦鍋ら柕濞垮労濞兼牜鎲搁悧鍫濈瑨婵鐓￠弻锝夊籍閸屾艾浠樼紓浣哄Т瀵爼濡甸崟顖氬嵆闁绘劖鎯屽Λ銈囩磽娴ｆ彃浜炬繝銏ｅ煐閸旀牠宕戦敐鍚ゅ綊鏁愰崨顓犻獓濠电偛鎳庨悧鎾诲蓟閿濆惟鐟滃秹寮稿☉銏＄厵妞ゆ棁宕甸惌娆忊攽閳╁啯鍊愬┑鈩冩倐婵偓闁绘ê宕ˉ姘舵⒒娴ｅ憡鍟炲〒姘殜瀹曪絾鎯旈埈銉秮閺屽棗顓奸崱娆忓箺闂備胶绮崝鏇烆嚕閸洖绠犻柣鏇犵＊a闂傚倷娴囬褏鈧稈鏅濈划娆撳箳濡炲皷鍋撻崘顔煎耿婵炴垼椴搁弲?
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ閵忊剝鐝曢梺鍝勬缁捇寮婚垾宕囨殼妞ゆ柨鍚嬮崳娲煙鐏炲倸鈧繈寮婚敐澶嬪亹闁告瑥顦遍妶顐︽⒒閸屾艾顏╅悗姘緲閻ｇ兘寮撮敍鍕澑闂佸搫鍊藉▔鏇犵玻閻愮儤鐓涘璺猴功婢ч亶鏌涚€ｎ亶妲兼い?",
        error,
      );
      throw error;
    }
  },

  // 2. 闂傚倸鍊烽懗鍫曗€﹂崼銏″床闁割偁鍎辩粈澶愭煙鏉堝墽鐣遍梻鍌ゅ灡缁绘稑顔忛鑽ょ泿闂佸憡顨嗘繛濠囧箖濡も偓閳藉鈻嶉搹顐㈢伌闁诡噯绻濋崺鈧?濠电姷鏁搁崕鎴犲緤閽樺褰掑磼閻愯尙鐛ュ┑掳鍊曢幊搴ㄥ几娓氣偓閺屻倕霉鐎ｎ偅婢掗梺绋款儐閹瑰洭骞冩禒瀣瀭妞ゆ劑鍨洪悾顒傜磽閸屾瑩妾烽柛鏂跨箳娴滅鈻庨幘铏€銈嗘磵閸嬫挾鈧鍠栭埀顒傚櫏濡查箖姊虹涵鍛棄缂佸缍婂濠氭晲婢跺á鈺呮煏婢跺牆鍔村ù鐘层偢濮婃椽宕妷銉ょ捕濡炪倖娲﹂崣鍐春閳?- PUT /api/v1/posts/{id}
  // 闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕曢幋锔界厽婵°倐鍋撻柣妤€锕ラ崚? { title, content }
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: PostDO }
  async updatePost(id, postData) {
    try {
      const response = await request(`/posts/${id}`, "PUT", postData, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞栭鈷氭椽濡舵径瀣槐闂侀潧艌閺呮盯鎷戦悢灏佹斀闁绘ɑ褰冮顏堟煛閸℃鐭掗柡灞糕偓宕囨殼妞ゆ柨鍚嬮崳娲煙鐏炲倸鈧牜鎹㈠┑瀣潊闁炽儱鍟挎慨鍝ョ磽娓氬洤鏋涘褌绮欓獮?",
        error,
      );
      throw error;
    }
  },

  // 3. 闂傚倸鍊风粈渚€骞夐敍鍕殰闁绘劕顕粻楣冩煃瑜滈崜姘辨崲濞戙垹宸濇い鎾跺枑瀹曟娊姊洪柅鐐茶嫰婢т即鏌℃担瑙勫€愭鐐诧躬楠炴﹢骞嗛弶鍟冿繝姊洪崫鍕潶闁稿孩鐓″畷鏇烆吋婢跺鍘遍梺鍝勬储閸斿本绂嶉悙鐑樼厽闁绘洑绀侀悘锕傛煙椤旂瓔娈旈柍钘夘槸閳诲氦绠涢幙鍐╃稇缂傚倸鍊峰鎺旀閿熺姴纾婚柛鏇ㄥ灠閻?- DELETE /api/v1/posts/{id}
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message }
  async deletePost(id) {
    try {
      const response = await request(`/posts/${id}`, "DELETE", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞夐敍鍕殰闁绘劕顕粻楣冩煃瑜滈崜姘辨崲濞戙垹宸濇い鎾跺枑瀹曟娊姊烘潪鎵窗闁革綇绲介锝夊磹閻曚焦鞋闂備胶顢婂Λ鍕矓閹绢喒鈧箓宕稿Δ鈧粻姘舵煙濞堝灝鏋ょ紓鍫ヤ憾閺?",
        error,
      );
      throw error;
    }
  },

  // 4. 闂傚倸鍊风粈浣虹礊婵犲偆鐒界憸蹇曟閻愬绡€闁搞儜鍥紬婵犵數鍋涘Ο濠冪濠婂牊瀚呴柣鏂垮悑閻撳繐顭块懜娈跨劷闁告ɑ鐩弻娑㈠Χ閸涱収浼冮梺璇″枙缁瑩銆佸☉妯锋婵﹩鍏橀崥鍌炴⒒娴ｅ搫鍔﹂柛鎾寸箞閹兘鍩℃担鐑樻濠殿喗锕╅崕鐢稿籍閸繄顦伴梺瀹狀潐閸庤櫕绂?- GET /api/v1/posts
  // 闂傚倸鍊风粈渚€骞栭銈嗗仏妞ゆ劧绠戠壕鍧楁煙缂併垹娅橀柡浣割儐娣囧﹪濡堕崨顔兼闂佹椿鍘介〃濠囧蓟濞戞矮娌柛鎾椻偓婵洤鈹? sectionId, page, size
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: PagePostDO }
  async getPosts(params = {}) {
    try {
      // 闂傚倸鍊风粈渚€骞栭锔绘晞闁告侗鍨崑鎾愁潩閻撳骸顫紓浣介哺閹瑰洭鐛Ο鍏煎珰闁艰壈鍩栭幉浼存⒒娓氣偓濞佳囁囨禒瀣亗闁割偁鍎遍崥褰掓煕閺囥劌鐏￠柣鎾存礃缁绘盯宕卞Δ鍐唶闂侀€炲苯澧紓宥咃工椤?
      const queryParams = new URLSearchParams();
      if (params.page) queryParams.append("page", params.page);
      if (params.size) queryParams.append("size", params.size);
      if (params.sectionId) queryParams.append("sectionId", params.sectionId);
      if (params.userId) queryParams.append("userId", params.userId);

      const endpoint = queryParams.toString()
        ? `/posts?${queryParams.toString()}`
        : "/posts";

      const response = await request(endpoint, "GET", null, "optional");
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ閵忊剝鐝曢梺鍝勬缁捇寮婚垾宕囨殼妞ゆ柨鍚嬮崳娲煙鐏炲倸鈧繂顫忓ú顏勫窛濠电姴鍊婚悷鎻掝渻閵堝啫濡兼い锕€鐏氭穱濠囧箻椤旇偐顦ㄩ梺瀹犳〃閼冲墎绮ｉ悙鐑樼厸濠㈣泛锕︽晶閬嶆煕鐎ｎ亶妲兼い?",
        error,
      );
      throw error;
    }
  },

  // 5. 闂傚倸鍊风粈渚€骞夐敍鍕殰婵°倕鍟伴惌娆撴煙鐎电啸缁?闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊搁崹鍌炴煟閵忋垺鏆╅柛妤佸哺閺岋綁骞嬮敐鍡╂濠碘槅鍋呴敃銏ゅ蓟閳╁啫绶為幖娣灮閵嗗﹪姊洪懡銈呮珡闁搞劌鐖奸獮?- POST /api/v1/posts
  // 闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕曢幋锔界厽婵°倐鍋撻柣妤€锕ラ崚? { sectionId, title, content }
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: PostDO }
  async createPost(postData) {
    try {
      const response = await request("/posts", "POST", postData, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞夐敍鍕殰婵°倕鍟伴惌娆撴煙鐎电啸缁惧彞绮欓弻鐔煎箲閹邦剛鍘梺鍝勬缁捇寮婚垾宕囨殼妞ゆ柨鍚嬮崳娲煙鐏炲倸鈧牜鎹㈠┑瀣潊闁炽儱鍟挎慨鍝ョ磽娓氬洤鏋涘褌绮欓獮?",
        error,
      );
      throw error;
    }
  },
};

// ==================== 濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸蹈閸ヮ剦鏁囬柕蹇曞Х閸旓箑顪冮妶鍡楃伇闁稿骸顭峰畷鏉款潡濮濈湋y-controller (闂傚倸鍊烽悞锕傚箖閸洖纾块柟鎯版绾剧粯绻涢幋鏃€鍤?闂傚倷娴囧畷鍨叏閺夋嚚娲Χ閸ワ絽浜炬慨妯煎帶閻忥附銇勯姀锛勬噰妤犵偛顑夐弫鍐焵椤掑倻涓嶇€规洖娲﹂崰鎰偓骞垮劚椤︻垶寮伴妷锔跨箚闁靛牆鎳忛崳褰掓⒒? ====================
export const replyApi = {
  // 1. 闂傚倸鍊风粈渚€骞栭銈嗗仏妞ゆ劧绠戠壕鍧楁煙缂併垹娅橀柡浣割儐娣囧﹪濡堕崨顓熸闁诲孩纰嶅畝鎼佸蓟閺囩喎绶為柛顐ｇ箓婵垽姊洪懡銈呮珡闁搞劌鐖奸獮鍐ㄎ旈埀顒勫煡婢跺ň鏋庨煫鍥ㄦ惄濞煎繒绱撻崒娆戝妽妞ゃ劌鎳橀獮濠傤潨閳ь剟鐛崱娑樼妞ゆ棁鍋愰ˇ鏉款渻閵堝棗濮傞柛銊潐鐎靛吋鎯旈妸锔规嫽婵炴挻鑹惧ú銈夊几閺冨牊鐓曢柡鍌濆劵鐎氫即鎮￠妶澶嬬厸鐎广儱鍟俊鑲╃磼?闂傚倷娴囧畷鍨叏閺夋嚚娲Χ閸ワ絽浜炬慨妯煎帶閻忥附銇勯姀锛勬噰妤犵偛顑夐弫鍐焵椤掑倻鐭嗛柛鈩兠肩换鍡涙煏閸繃鍣规い蹇嬪劦閺?- GET /api/v1/posts/{postId}/replies
  // 闂傚倸鍊风粈渚€骞栭銈嗗仏妞ゆ劧绠戠壕鍧楁煙缂併垹娅橀柡浣割儐娣囧﹪濡堕崨顔兼闂佹椿鍘介〃濠囧蓟濞戞矮娌柛鎾椻偓婵洤鈹? page, size
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: PageReplyDO }
  async getRepliesByPostId(postId, params = {}) {
    try {
      // 闂傚倸鍊风粈渚€骞栭锔绘晞闁告侗鍨崑鎾愁潩閻撳骸顫紓浣介哺閹瑰洭鐛Ο鍏煎珰闁艰壈鍩栭幉浼存⒒娓氣偓濞佳囁囨禒瀣亗闁割偁鍎遍崥褰掓煕閺囥劌鐏￠柣鎾存礃缁绘盯宕卞Δ鍐唶闂侀€炲苯澧紓宥咃工椤?
      const queryParams = new URLSearchParams();
      if (params.page) queryParams.append("page", params.page);
      if (params.size) queryParams.append("size", params.size);

      const endpoint = queryParams.toString()
        ? `/posts/${postId}/replies?${queryParams.toString()}`
        : `/posts/${postId}/replies`;

      const response = await request(endpoint, "GET", null, "optional");
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╅梺缁樺姇閿曨亪寮婚敐澶婄疀妞ゆ帊璁查弸娆忊攽閳╁啫绲绘い顓犲厴瀵鈽夐姀鐘殿唺闂佺懓鐏濋崯顖炴偩娴犲鈷戦梻鍫熺⊕椤ユ粓鏌涢悤浣哥仩妞ゆ洏鍎靛畷鐔碱敆娴ｈ櫣肖婵＄偑鍊栭崝鎴﹀磿?",
        error,
      );
      throw error;
    }
  },

  // 2. 闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊搁崹鍌炴煟閵忋垺鏆╅柛妤佸哺閺岋綁骞嬮敐鍡╂濠碘槅鍋呴敃銏ゅ蓟閻斿皝鏋旈柛顭戝枟閻忓秹姊烘导娆戠М濞存粌鐖奸獮鍐亹閹烘垹鍊為悷婊冪Ч椤㈡棃顢楁担鍏哥盎?闂傚倸鍊烽悞锕傚箖閸洖纾块柟鎯版绾剧粯绻涢幋鏃€鍤?- POST /api/v1/posts/{postId}/replies
  // 闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕曢幋锔界厽婵°倐鍋撻柣妤€锕ラ崚? { content }
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: ReplyDO }
  async createReply(postId, replyData) {
    try {
      const userId = authApi.getUserIdFromToken();
      const requestData = {
        ...replyData,
        userId,
      };
      const response = await request(
        `/posts/${postId}/replies`,
        "POST",
        requestData,
        true,
      );
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞夐敍鍕殰婵°倕鍟伴惌娆撴煙鐎电啸缁惧彞绮欓弻鐔煎箲閹伴潧娈梺缁樺姇閿曨亪寮婚敐澶婄疀妞ゆ帊璁查弸娆忊攽闄囩亸顏堫敋瑜旈垾锕傚锤濡も偓缁犳岸鏌熷▓鍨灓缂傚牓浜堕弻?",
        error,
      );
      throw error;
    }
  },

  // 3. 闂傚倸鍊风粈渚€骞夐敍鍕殰闁绘劕顕粻楣冩煃瑜滈崜姘辨崲濞戙垹宸濇い鎾跺枑瀹曟娊姊洪柅鐐茶嫰婢т即鏌℃担瑙勫€愭鐐诧躬楠炴﹢骞嗛弶鍟冿繝姊洪崫鍕潶闁稿孩鐓″畷鏇烆吋婢跺鍘遍梺鍝勬储閸斿矂寮搁妶鍡曠箚闁告瑥顦悘鎾煙?闂傚倸鍊烽悞锕傚箖閸洖纾块柟鎯版绾剧粯绻涢幋鏃€鍤嶉柛銉墮閽冪喖鏌曟径鍫濆姢妞わ负鍔戝娲川婵犱胶绻侀梺鍛婄懃闁帮絽鐣?- DELETE /api/v1/replies/{id}
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message }
  async deleteReply(id) {
    try {
      const response = await request(`/replies/${id}`, "DELETE", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞夐敍鍕殰闁绘劕顕粻楣冩煃瑜滈崜姘辨崲濞戙垹宸濇い鎾卞灩瀵即鎮楃憴鍕闁绘牕銈搁獮鍐ㄢ枎韫囷絽鏅犲銈嗘煥瑜般劑鍩￠崒婊呯槇闂侀潧绻嗛埀顒€鍟挎慨鍝ョ磽娓氬洤鏋涘褌绮欓獮?",
        error,
      );
      throw error;
    }
  },
};

// ==================== 闂傚倸鍊烽悞锕傚箖閸洖纾块柤纰卞墰閻瑩鐓崶銊р槈闁绘帒鐏氶妵鍕箣濠靛牅铏庡銇卞啫鐏砪tion-controller (闂傚倸鍊风粈渚€骞栭位鍥敍閻愭潙鈧埖绻濋棃娑欙紞闁?闂傚倸鍊风粈渚€骞夐敍鍕殰闁圭儤鍤﹀☉妯滄棃宕樿閸撱劌顪冮妶鍡欏⒈闁稿绋撶划璇茬暦閸ャ劌鏋戦悗骞垮劚椤︻垶寮伴妷锔跨箚闁靛牆鎳忛崳褰掓⒒? ====================
export const sectionApi = {
  // 1. 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╃紓浣插亾闁稿瞼鍋為悡蹇撯攽閻愰潧浜炬繛鍛嚇閺屾盯寮拠鎻掑Е闂佸搫鏈惄顖炵嵁閹烘绠ｉ柣鎴濇椤ワ綁姊?闂傚倸鍊风粈渚€骞栭位鍥敍閻愭潙鈧埖绻濋棃娑欙紞闁绘帊绮欓弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩鎾Ω閵夈倗鐩庨梻浣筋嚙缁绘垿鎮烽埡鍛畺?- GET /api/v1/sections
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: SectionDO[] }
  async getAllSections() {
    try {
      const response = await request("/sections", "GET", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╃紓浣哄珡閸ャ劎鍘卞銈庡幗閸ㄧ敻寮搁悢鍏肩厱閻庯絻鍔岄崝锕傛煛鐏炲墽娲存鐐村浮楠炴瑩宕橀埡鍐╂瘜闂傚倷绶氬褍螞濡ゅ懎鐤い鏍ㄥ嚬閸ゆ鏌涢弴銊ュ缂傚秴娲﹂妵鍕箛閳轰礁澹?",
        error,
      );
      throw error;
    }
  },

  // 2. 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╃紒缁㈠幐閸嬫捇姊绘担鍛婃儓婵炶绠戦悾婵嬪箹娴ｇ鎯為柣搴秵閸犳鎮″☉姗嗙唵閻犺櫣灏ㄩ崝鐔访归悩顔肩仾缂佺粯绋撳☉鍨槹鎼达絿妲囬梻浣筋嚃閸ㄤ即宕弶鎴犳殾闁绘梻鈷堥弫瀣煕濞戝崬骞楅柣銈勮兌缁辨捇宕掑顑藉亾妞嬪海鐭嗗ù锝堝€藉☉銏犵睄闁逞屽墰缁碍娼忛妸褏鐦堥梺鎼炲劀閸涱垱娈?- GET /api/v1/sections/{id}
  // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝濠傜墕閽冪喖鏌曟繛鍨壄婵炲樊浜滈崘鈧銈嗘尵閸嬫盯宕妸鈺傗拺缂備焦蓱鐏忣厽绻涢幘顕呮婵炴垹鏁婚崺鈧? { code, message, data: SectionDO }
  async getSectionById(id) {
    try {
      const response = await request(`/sections/${id}`, "GET", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╃紓浣哄珡閸ャ劎鍘卞銈庡幗閸ㄧ敻寮搁悢鍏肩厱閻庯絻鍔岄崝锕傛煙椤旇偐绉虹€规洖鐖奸幃婊堟嚍閵夘喒鍋撳澶嬧拺缂佸娼￠妤冪磼缂佹ê鐏ユい鏇樺劦瀹曠喖顢楁担铏剐ゆ俊鐐€栭崝鎴﹀磿?",
        error,
      );
      throw error;
    }
  },
};

// ==================== 濠电姷鏁搁崑娑㈡偤閵娧冨灊鐎广儱顦拑鐔兼煥濠靛棭妲搁柣鎺戠仛閵囧嫰骞嬮敐鍛Х閻庢鍠氶ˉ娓唕-controller (闂傚倸鍊烽悞锕€顪冮崹顕呯劷闁秆勵殔缁€澶屸偓骞垮劚椤︻垶寮伴妷锔剧闁瑰鍋為惃鎴濃槈閹惧磭效闁哄被鍔岄埥澶娢熸笟顖欑棯闂備胶顭堥敃锕傚磻閵堝钃熼柣鏃傚劋鐏忔帒顭跨捄鐚村伐闁哄棙鐟╅弻锝夊箻閺夋垹浼岄梺? ====================
export const userApi = {
  // 1. 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旇崵鏆楁繛瀛樼矊缂嶅﹪寮婚悢鐓庣畾鐟滃秹寮虫潏銊ｄ簻闁靛牆鎳忛崵鍥煛瀹€瀣М闁挎繄鍋ら、妤呭焵椤掍椒绻嗗ù鐘差儐閻撴洟鏌嶉崫鍕偓濠氬Υ閹烘鐓欑紒澶婃濞层倗绮堢€ｎ偁浜滈柟鍝勭Х閸忓苯霉?- GET /api/v1/users/profile
  async getCurrentProfile() {
    try {
      const response = await request("/users/profile", "GET", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旇崵鏆楁繛瀛樼矊缂嶅﹪寮婚悢鐓庣畾鐟滃秹寮虫潏銊ｄ簻闁靛牆鎳忛崵鍥煛瀹€瀣М闁挎繄鍋ら、妤呭焵椤掍椒绻嗗ù鐘差儐閻撴洟鏌嶉崫鍕偓濠氬Υ閹烘鐓欑紒澶婃濞层倗绮堢€ｎ偁浜滈柟鍝勭Х閸忓苯霉濠婂棭娼愮紒缁樼箞婵偓闁炽儱鍟挎慨鍝ョ磽娓氬洤鏋涘褌绮欓獮?",
        error,
      );
      throw error;
    }
  },

  // 2. 闂傚倸鍊风粈渚€骞栭鈷氭椽濡舵径瀣槐闂侀潧艌閺呮盯鎷戦悢灏佹斀闁绘ê寮舵径鍕煕鐎ｃ劌濮傞柡灞炬礃缁绘繆绠涢弴鐘虫闂備胶绮幐璇裁洪悢鐓庣畺缁绢厼鎷嬪Σ濠氭煟鎼淬垹鍤柛娆忓暙椤?- PUT /api/v1/users/profile
  async updateProfile(profileData) {
    try {
      const response = await request(
        "/users/profile",
        "PUT",
        profileData,
        true,
      );
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞栭鈷氭椽濡舵径瀣槐闂侀潧艌閺呮盯鎷戦悢灏佹斀闁绘ê寮舵径鍕煕鐎ｃ劌濮傞柡灞炬礃缁绘繆绠涢弴鐘虫闂備胶绮幐璇裁洪悢鐓庣畺缁绢厼鎷嬪Σ濠氭煟鎼淬垹鍤柛娆忓暙椤曪綁骞栨担鍝ュ姶闂佸憡鍔戦崝搴ｇ玻閻愮儤鐓涘璺猴功婢ч亶鏌涚€ｎ亶妲兼い?",
        error,
      );
      throw error;
    }
  },

  // 3. 濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸劍閺呮繈鏌ㄥ┑鍡樺妞ゎ偅娲熼弻銈夊箹娴ｈ閿紓浣稿閸嬨倝寮婚埄鍐ㄧ窞閻庯綆浜炴禒鎾⒑?- POST /api/v1/users/avatar
  async uploadAvatar(file) {
    try {
      const formData = new FormData();
      formData.append("file", file);

      const url = `${API_BASE_URL}/users/avatar`;
      const token = getAuthToken();
      const headers = {};
      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }

      const response = await fetch(url, {
        method: "POST",
        headers,
        body: formData,
        credentials: "include",
      });

      const responseData = await response.json();
      if (!response.ok) {
        throw new ApiError(
          responseData.message || "Upload avatar failed",
          response.status,
        );
      }
      return responseData;
    } catch (error) {
      console.error(
        "濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸劍閺呮繈鏌ㄥ┑鍡樺妞ゎ偅娲熼弻銈夊箹娴ｈ閿紓浣稿閸嬨倝寮婚埄鍐ㄧ窞閻庯綆浜炴禒鎾⒑閸涘﹥灏甸柛妯犲洠鈧箓宕稿Δ鈧粻姘舵煙濞堝灝鏋ょ紓鍫ヤ憾閺?",
        error,
      );
      throw error;
    }
  },

  // 4. 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╃紓浣哄С閸楁娊寮诲☉銏犖ㄩ柟瀛樼箖閸ｇ儤鎱ㄩ敐鍛村弰婵﹤鎼叅缂備焦锚閳诲繘姊洪崨濠冪叆缂佸鎳撻悾鐑藉传閸曘劍顫嶅┑顔斤供閸樺ジ鎯冮锔解拺闁告稑锕ユ径鍕煕閹炬潙鍝洪柟?- GET /api/v1/users/{userId}/profile
  async getUserProfile(userId) {
    try {
      const response = await request(
        `/users/${userId}/profile`,
        "GET",
        null,
        true,
      );
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫梺绋款儍閸旀垿寮婚弴鐔虹瘈闊洦娲滈弳鐘绘⒑缂佹ɑ灏版繛鑼枛楠炲啰娑甸崪浣剐╅梺璇插閸戝綊宕ｉ崘顭戝殨闁归棿绀侀悞娲煕閹扳晛濡跨紒鎰仱閺屸剝寰勬繝鍕拡闂佺顑呴ˇ鎶铰?",
        error,
      );
      throw error;
    }
  },
};

// ==================== 闂傚倸鍊烽懗鍫曗€﹂崼婢濈懓顫濈捄鍝勫亶閻熸粎澧楃敮鎺楁倿閸偁浜滈柟瀛樼箘娴犮垽鎮楅敐鍌氫壕ike-controller (闂傚倸鍊烽懗鍓佸垝椤栫偛绀夋俊銈呮噷閳ь兛鐒︾换婵嬪礋椤撶姷鍔归梻浣告贡婢ф顭垮Ο鑲╀笉鐎规洖娲﹂崰鎰偓骞垮劚椤︻垶寮伴妷锔跨箚闁靛牆鎳忛崳褰掓⒒? ====================
export const likeApi = {
  // 闂傚倸鍊烽懗鍓佸垝椤栫偛绀夋俊銈呮噷閳ь兛鐒︾换婵嬪礋椤撶姷鍔?- POST /api/v1/likes
  // 闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕曢幋锔界厽婵°倐鍋撻柣妤€锕ラ崚? { targetType, targetId }
  // targetType: "post" | "reply"
  async like(likeData) {
    try {
      const response = await request("/likes", "POST", likeData, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊烽懗鍓佸垝椤栫偛绀夋俊銈呮噷閳ь兛鐒︾换婵嬪礋椤撶姷鍔归梻浣哥秺閸嬪﹪宕㈤懖鈺傤偨闁绘劕鎼痪褔鏌涢顐簻濞存粠鍨抽幃?",
        error,
      );
      throw error;
    }
  },

  // 闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊归崑瀣繆閵堝懎鏆熼柣顓熸崌閺岋綁骞嬮敐鍛呮捇鏌涙繝鍥舵闁汇儺浜、姗€鎮欓弶鎴烆仱婵?- DELETE /api/v1/likes
  // 闂傚倸鍊风粈渚€骞栭銈嗗仏妞ゆ劧绠戠壕鍧楁煙缂併垹娅橀柡浣割儐娣囧﹪濡堕崨顔兼闂佹椿鍘介〃濠囧蓟濞戞矮娌柛鎾椻偓婵洤鈹? targetType, targetId
  async unlike(targetType, targetId) {
    try {
      const queryParams = new URLSearchParams();
      queryParams.append("targetType", targetType);
      queryParams.append("targetId", targetId);

      const endpoint = `/likes?${queryParams.toString()}`;
      const response = await request(endpoint, "DELETE", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊归崑瀣繆閵堝懎鏆熼柣顓熸崌閺岋綁骞嬮敐鍛呮捇鏌涙繝鍥舵闁汇儺浜、姗€鎮欓弶鎴烆仱婵＄偑鍊愰弲鐘差焽瑜旈垾锕傚锤濡も偓缁犳岸鏌熷▓鍨灓缂傚牓浜堕弻?",
        error,
      );
      throw error;
    }
  },
};

// ==================== 濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸閻ゎ噣鏌曟繛鐐珔闁绘帒鐏氶妵鍕箣閿濆懎濮烽柣搴㈢閻涱柡ification-controller (闂傚倸鍊搁崐椋庢閿熺姴纾婚柛娑卞弾濞尖晠鏌曟繛鐐珕闁稿鍊块弻銊╂偄閸濆嫅銏㈢磼閻樿尙锛嶉柟鑼焾椤撳吋寰勬繝鍐┬氭繝寰锋澘鈧洟骞婃惔銊︹拻? ====================
export const notificationApi = {
  // 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫梺缁樺笧缁垶骞堥妸銉庣喐寰勭粙鎸庡創闂備礁鎲￠悷銏ゅ磻閹剧粯鈷掑ù锝呮啞閸熺偤鎮介娑辨疁闁靛棗鍟鍕箛椤?- GET /api/v1/notifications
  async getNotifications(params = {}) {
    try {
      const queryParams = new URLSearchParams();
      if (params.page) queryParams.append("page", params.page);
      if (params.size) queryParams.append("size", params.size);

      const endpoint = queryParams.toString()
        ? `/notifications?${queryParams.toString()}`
        : "/notifications";

      const response = await request(endpoint, "GET", null, true);
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫梺缁樺笧缁垶骞堥妸銉庣喐寰勭粙鎸庡創闂備礁鎲￠悷銏ゅ磻閹剧粯鈷掑ù锝呮啞閸熺偤鎮介娑辨疁闁靛棗鍟鍕箛椤戔斂鍎甸弻娑㈠箛閵婏附鐝旂紓浣稿閸嬨倕顕ｉ崼鏇為唶妞ゆ劦婢€閸戜粙鎮?",
        error,
      );
      throw error;
    }
  },

  // 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╅柣搴㈣壘椤︿即濡甸崟顔剧杸闁规儳顕妶鈺呮⒑閹肩偛鈧垿宕归挊澶樻綎婵炲樊浜堕弫鍐煥濠靛棙顥犳繛鍫涘€濆铏圭磼濡粯鍎撶紓浣虹帛缁诲倿顢?- GET /api/v1/notifications/unread-count
  async getUnreadCount() {
    try {
      const response = await request(
        "/notifications/unread-count",
        "GET",
        null,
        true,
      );
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╅柣搴㈣壘椤︿即濡甸崟顔剧杸闁规儳顕妶鈺呮⒑閹肩偛鈧垿宕归挊澶樻綎婵炲樊浜堕弫鍐煥濠靛棙顥犳繛鍫涘€濆铏圭磼濡粯鍎撶紓浣虹帛缁诲倿顢氶敐澶樻晝闁挎洍鍋撶紒鐙欏洦鍋″ù锝夋涧閳ь剚鎹囧鍫曨敇閵忥紕鍘?",
        error,
      );
      throw error;
    }
  },

  // 闂傚倸鍊风粈渚€骞栭銈囩煋闁哄鍤氬ú顏勭厸闁告粈鐒﹂弲鈺呮⒑閹肩偛鍔撮柛鎾村哺閹偟绱掑Ο璇插伎濠碘槅鍨伴妵姗€宕ラ锔界厱婵炲棗鑻禍鐐節閻㈤潧浠﹂柛銊ョ埣楠炴劙骞栨担鍝ヮ啇濡炪倖鍔х粻鎴犵矆閸曨垱鐓ユ繛鎴灻顏堟倵?- POST /api/v1/notifications/{notificationId}/read
  async markRead(notificationId) {
    try {
      const response = await request(
        `/notifications/${notificationId}/read`,
        "POST",
        null,
        true,
      );
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞栭銈囩煋闁哄鍤氬ú顏勭厸闁告粈鐒﹂弲鈺呮⒑閹肩偛鍔撮柛鎾村哺閹偟绱掑Ο璇插伎濠碘槅鍨伴妵姗€宕ラ锔界厱婵炲棗鑻禍楣冩⒑鐠囧弶鍞夋い顐㈩樀閹椽寮撮悩杈╁墾濡炪倕绻愰悧婊堝极鐎ｎ喗鐓熸俊銈傚亾闁绘妫涘▎銏ゆ倷閸濆嫮楠囬梺鍓插亽閸嬪嫭绂嶉婊勫仏?",
        error,
      );
      throw error;
    }
  },

  // 闂傚倸鍊风粈渚€骞栭銈囩煋闁哄鍤氬ú顏勭厸闁告粈鐒﹂弲鈺呮⒑閹肩偛鍔撮柛鎾寸懅缁濡烽埡鍌滃帗閻熸粍绮撳畷婊堟偄婵傚娈ㄩ梺鍓茬厛閸嬪嫮娆㈤悙鐑樼厵闁煎壊鍓欐俊鐓幟瑰鍛壕闁逛究鍔岃灒濠㈠墎顭堥幆鍫ユ⒑閸涘﹦鎲块柛瀣尭閳规垿鎮欓弶鎴犱桓闂佺懓鎲￠幃鍌炵嵁閺嶎収鏁冮柨鏇楀亾缂佲偓閸曨垱鐓ユ繛鎴灻顏堟倵?- POST /api/v1/notifications/read-all
  async markAllRead() {
    try {
      const response = await request(
        "/notifications/read-all",
        "POST",
        null,
        true,
      );
      return response;
    } catch (error) {
      console.error(
        "闂傚倸鍊风粈渚€骞栭銈囩煋闁哄鍤氬ú顏勭厸闁告粈鐒﹂弲鈺呮⒑閹肩偛鍔撮柛鎾寸懅缁濡烽埡鍌滃帗閻熸粍绮撳畷婊堟偄婵傚娈ㄩ梺鍓茬厛閸嬪嫮娆㈤悙鐑樼厵闁煎壊鍓欐俊鐓幟瑰鍛壕闁逛究鍔岃灒濠㈠墎顭堥幆鍫ユ⒑閸涘﹦鎲块柛瀣崌閺岋絾鎯旈妶搴㈢秷闂佺儵鍓濆ú婊勭閹间緡鏁傞柛顐亝閺咁亪姊虹紒姗嗘當闁绘妫涘▎銏ゆ倷閸濆嫮楠囬梺鍓插亽閸嬪嫭绂嶉婊勫仏?",
        error,
      );
      throw error;
    }
  },
};

// 闂傚倸鍊烽懗鍫曞储瑜旈妴鍐╂償閵忋埄娲稿┑鐘诧工鐎氼參宕ｈ箛娑欑厓闁告繂瀚崳鍦磼閳ь剟宕卞☉娆屾嫼闂佸壊鐓堥崳顕€宕曢幇鐗堢厽闁圭虎鍨版禍鐐節閻㈤潧浠滄い鏇ㄥ幗閹便劍瀵奸弶鎴狅紮濠德板€撻悞锕€鈻?- 闂傚倸鍊风粈渚€骞夐敓鐘冲仭妞ゆ牜鍋涢崹鍌涖亜閺嶃劎銆掓い鈺傜叀閺岀喖鎮滃鍡樼暥闂佸憡鍔忛崑鎾绘⒑閼姐倕鏋戦柣鐔村劤閳ь剚鍑归崜娑氬垝婵犲洦鏅濋柛灞剧▓閹峰姊洪幖鐐插姷缂佽尪濮ょ粋宥嗐偅閸愨晝鍘介梺缁樻煥閹诧紕澹曠紒妯圭箚闁绘劖娼欑粭姘辩磼缂佹娲寸€规洖鐖奸、妤佹媴闂€鎰处?
let refreshTimeout;

// 闂傚倷娴囧畷鍨叏瀹曞洨鐭嗗ù锝堫潐濞呯姴霉閻樺樊鍎愰柛瀣典邯閺屾盯鍩勯崘鍓у姺濡炪倐鏅濋崗姗€寮婚弴銏犻唶婵犻潧娲﹂幃娆忣渻閵堝啫鈧洟宕愰崸妤€钃熼柕鍫濇闂勫嫬顭跨捄渚Ш闁烩晛瀛╃换婵嗏枔閸喗鐏€闂佸搫鎳愭慨鎾偩瀹勯偊娼╅柤鍝ユ暩閸欏棝鏌ｆ惔顖滅У闁稿妫濆畷?
export function setupAutoRefresh() {
  // 婵犵數濮烽弫鎼佸磻閻愬搫绠伴柟闂寸缁犵姵淇婇婵勨偓鈧柡瀣Ч楠炴牕菐椤掆偓婵′粙鏌涚€ｎ偆绠為柡宀嬬到铻栭柍褜鍓熼幃褎绻濋崟顓濈瑝闂佸湱鍎ら〃鍡涙偂閻斿吋鐓忓┑鐐茬仢閸旀碍銇勮箛濠冩珚闁哄本鐩崺锟犲磼濠婂嫬鍨遍柣搴㈩問閸ｎ噣宕滈悢濂夊殨闁告挷鐒﹀畷澶愭煏婵犲繘妾柣锔惧仱濮婄粯鎷呴崨濠冨創闂佸摜鍠庨崯鍧楀煝閺傚簱妲堟俊顖炴敱閻?
  if (refreshTimeout) {
    clearTimeout(refreshTimeout);
  }

  // 闂傚倷娴囧畷鍨叏瀹曞洨鐭嗗ù锝堫潐濞呯姴霉閻樺樊鍎愰柛瀣典邯閺屾盯鍩勯崘顏佹濠碘槅鍋呴敃銏ゅ蓟閻斿皝鏋旈柛顭戝枟閻忓秹姊烘导娆戠М濞存粌鐖煎璇测槈閵忕姷顔婂┑掳鍊撻悞锕傚汲閻樼粯鈷戦柛婵嗗鐎氫即鏌ㄩ弴顏嗙暤闁糕斁鍋撳銈嗗笒閸犳艾顭囬幇顔炬／闁哄娉曞瓭濡炪値浜滈崯鏉戠暦閹烘鍊烽柡灞诲劜閿涙淇婇悙顏勨偓銈夊储閻ｅ本鏆滈柣鎰暩閻棝鏌涢弴銊ョ仭闁绘挻娲熼弻銊╁棘閹稿孩鍎撴繝纰樺墲濡炰粙寮婚悢鑲╁彆闁圭粯宸婚崑鎾斥攽鐎ｎ亞鐣洪悷婊冪Ч閹儳鈹戠€ｎ亞顦ㄥ銈呯箰濡盯寮抽锔解拻濞达絽鎲￠幆鍫ユ煟濡も偓缁绘ê鐣烽悧鍫熷劅闁靛繆鍓濋悵?闂傚倸鍊风粈渚€骞夐敍鍕殰闁圭儤鍤﹀☉妯锋斀閻庯綆浜為鍥⒑鐟欏嫬绀冩い鏇嗗懐鐭嗛柛鈩冪⊕閳锋垿鏌涢…鎴濇珮闁稿孩鍔欓弻锝夊箻椤栨矮澹曢梻?
  const refreshInterval = 25 * 60 * 1000; // 25闂傚倸鍊风粈渚€骞夐敍鍕殰闁圭儤鍤﹀☉妯锋斀閻庯綆浜為?
  refreshTimeout = setTimeout(async () => {
    try {
      await authApi.refreshToken();
      // 闂傚倸鍊风粈渚€骞夐敍鍕殰闁跨喓濮寸紒鈺呮⒑椤掆偓缁夋挳鎷戦悢灏佹斀闁绘ê寮堕幖鎰磼閻欏懐绉柡宀€鍠栭弻鍥晝閳ь剚鏅堕濮愪簻妞ゆ劑鍊曢埢鍫ユ煛鐏炲墽鈽夋い顐ｇ箞椤㈡鍩€椤掑嫬鍚归弶鍫氭櫇绾惧ジ寮堕崼娑樺缂佺姷鍋ら弻锛勪沪閸婄喎顏銈嗘尭閵堢鐣烽崡鐐嶇喖鎮℃惔锛勭憿闂傚倷娴囧畷鍨叏瀹曞洨鐭嗗ù锝堫潐濞呯姴霉閻樺樊鍎愰柛瀣典邯閺屾盯鍩勯崘顏呭枦闂佺顑嗛幐楣冨箟閹绢喖绀嬫い鎾楀嫭顔愬┑鐘殿暜缁辨洟宕戝☉銏犵９闁割煈鍠氶弳?
      setupAutoRefresh();
    } catch (error) {
      console.error(
        "闂傚倸鍊烽懗鍫曞储瑜旈妴鍐╂償閵忋埄娲稿┑鐘诧工鐎氼參宕ｈ箛娑欑厓闁告繂瀚崳鍦磼閳ь剟宕卞☉娆屾嫼闂佸壊鐓堥崳顕€宕曢幇鐗堢厽闁圭虎鍨版禍鐐節閻㈤潧浠滄い鏇ㄥ幗閹便劍瀵奸弶鎴狅紮濠德板€撻悞锕€鈻嶉悩宕囩瘈闂傚牊绋掗幖鎰磼閸撲礁浠辩€殿喖鐖煎畷濂割敃椤厼鍤遍柣?",
        error,
      );
    }
  }, refreshInterval);
}

// 闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊归崑瀣繆閵堝懎鏆熼柣顓熸崌閺岋綁骞嬮敐鍛呮捇鏌涢妶鍜冩敾闁靛洤瀚粻娑㈠箻閹颁椒鎮ｉ柣鐐存皑閸犳牕顫忓ú顏勫窛濠电姴鍊圭拠鐐烘⒑鐞涒€充壕闂備緡鍓欑粔鎾嫅?
export function cancelAutoRefresh() {
  if (refreshTimeout) {
    clearTimeout(refreshTimeout);
    refreshTimeout = null;
  }
}

// 濠电姵顔栭崰妤冩暜濡ゅ啰鐭欓柟鐑樸仜閳ь剨绠撳畷濂稿Ψ椤旇姤娅嶉梻浣哄帶椤洟宕愰幇鏉跨；闁瑰墽绮崑銊╂⒒閸喓鈽夊ù鐘筹耿濮婃椽骞栭悙娴嬪亾閵堝鐭楅柛鎰靛枤瀹撲胶鈧箍鍎遍ˇ顖炴倿閸偁浜滈柟鐑樺灥椤忊晠鎮楀顒夋█闁哄本鐩、鏇㈠Χ閸モ晜鍟碔
export default {
  auth: authApi,
  post: postApi,
  reply: replyApi,
  section: sectionApi,
  user: userApi,
  like: likeApi,
  notification: notificationApi,
};
