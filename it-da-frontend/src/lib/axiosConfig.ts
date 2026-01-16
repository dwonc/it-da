import axios from "axios";

// axios 전역 설정
axios.defaults.withCredentials = true;
axios.defaults.baseURL = "http://localhost:8080";

export default axios;
