import axios from "axios";

const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

const token = localStorage.getItem("token");
if (token) {
  apiClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
}

// Register User
export const registerUser = async (
  username: string,
  email: string,
  password: string
) => {
  try {
    return await apiClient.post("/register", {
      username: username.toLowerCase(),
      email: email.toLowerCase(),
      password,
    });
  } catch (error) {
    throw error;
  }
};

// Login User
export const loginUser = async (email: string, password: string) => {
  try {
    const response = await apiClient.post("/login", {
      email: email.toLowerCase(),
      password,
    });

    const { token } = response.data;
    if (token) {
      localStorage.setItem("token", token);
      apiClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    }

    return response.data;
  } catch (error) {
    throw error;
  }
};

// Google Login
export const googleLogin = async () => {
  window.location.href = `${API_BASE_URL}/auth/login`;
};

// Fetch Recent Searches
export const fetchRecentSearches = async () => {
  try {
    return await apiClient.get("/recent_searches");
  } catch (error) {
    throw error;
  }
};

// Save Recent Search
export const saveSearch = async (query: string) => {
  try {
    return await apiClient.post("/recent_searches", {
      query: query.toLowerCase(),
    });
  } catch (error) {
    throw error;
  }
};

// Delete Recent Search
export const deleteSearch = async (searchId: number) => {
  try {
    return await apiClient.delete(`/recent_searches/${searchId}`);
  } catch (error) {
    throw error;
  }
};

export const imageSearch = async (param: any) => {
  try {
    return await apiClient.get(
      `/images?q=${param.query.length > 0 ? param.query : "random"}&page=${
        param.page
      }`
    );
  } catch (error) {
    throw error;
  }
};

export const imageDetail = async (param: any) => {
  try {
    return await apiClient.get(`/images/${param}`);
  } catch (error) {
    throw error;
  }
};

export const audioSearch = async (param: any) => {
  try {
    return await apiClient.get(
      `/audios?q=${param.query.length > 0 ? param.query : "random"}&page=${
        param.page
      }`
    );
  } catch (error) {
    throw error;
  }
};

export const audioDetail = async (param: any) => {
  try {
    return await apiClient.get(`/audio/${param}`);
  } catch (error) {
    throw error;
  }
};
