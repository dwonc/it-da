// src/api/http.ts
import axios from "axios";

/**
 * 프로젝트가 쿠키 세션 기반이면 withCredentials=true가 필요합니다.
 * Authorization Bearer 토큰 기반이면 인터셉터에서 헤더를 붙이세요.
 */
export const http = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080",
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
});

// 토큰 기반이라면 여기서 붙이세요.
// http.interceptors.request.use((config) => {
//   const token = localStorage.getItem("accessToken");
//   if (token) config.headers.Authorization = `Bearer ${token}`;
//   return config;
// });
