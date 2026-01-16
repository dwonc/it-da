import { create } from "zustand";
import { persist } from "zustand/middleware";
import axios from "axios";

interface Meeting {
  meetingId: number;
  title: string;
  description: string;
  category: string;
  subcategory: string;
  locationName: string;
  meetingTime: string;
  maxParticipants: number;
  currentParticipants: number;
  expectedCost: number;
  vibe: string;
  imageUrl?: string;
  avgRating?: number;
  organizerId: number;
}

interface RecentItem {
  id: number;
  icon: string;
  title: string;
  time: string;
  type: "chat" | "meeting";
}

interface MeetingStore {
  // State
  meetings: Meeting[];
  recentItems: RecentItem[];
  aiRecommendation: Meeting | null;
  selectedCategory: string;
  searchQuery: string;
  isLoading: boolean;

  // Actions
  fetchMeetings: () => Promise<void>;
  fetchRecentItems: () => Promise<void>;
  fetchAIRecommendation: (userId: number) => Promise<void>;
  setCategory: (category: string) => void;
  setSearchQuery: (query: string) => void;
  searchMeetings: (query: string) => Promise<void>;
}

const API_BASE_URL = "http://localhost:8080/api";

const normalizeMeeting = (m: any) => ({
  meetingId: m.meetingId ?? m.meeting_id,
  title: m.title,
  description: m.description,
  category: m.category,
  subcategory: m.subcategory,
  locationName: m.locationName ?? m.location_name,
  meetingTime: m.meetingTime ?? m.meeting_time,
  maxParticipants: m.maxParticipants ?? m.max_participants,
  currentParticipants: m.currentParticipants ?? m.current_participants,
  expectedCost: m.expectedCost ?? m.expected_cost,
  vibe: m.vibe,
  imageUrl: m.imageUrl ?? m.image_url,
  avgRating: m.avgRating ?? m.avg_rating,
  organizerId: m.organizerId ?? m.organizer?.user_id ?? m.organizer?.userId,
});

export const useMeetingStore = create<MeetingStore>()(
  persist(
    (set, get) => ({
      // Initial State
      meetings: [],
      recentItems: [],
      aiRecommendation: null,
      selectedCategory: "전체",
      searchQuery: "",
      isLoading: false,

      // Fetch all meetings
      fetchMeetings: async () => {
        set({ isLoading: true });
        try {
          const response = await axios.get(`${API_BASE_URL}/meetings`);

          // ✅ Spring Boot 응답 구조 처리
          // { success, message, meetings: [...], totalCount }
          const meetingsData = response.data.meetings || response.data || [];

          set({
            meetings: Array.isArray(meetingsData)
              ? meetingsData.map(normalizeMeeting)
              : [],
            isLoading: false,
          });

          console.log("📦 API Response:", response.data);
          console.log("✅ Meetings 추출:", meetingsData);

          set({
            meetings: Array.isArray(meetingsData) ? meetingsData : [],
            isLoading: false,
          });
        } catch (error) {
          console.error("❌ 모임 조회 실패:", error);
          set({ meetings: [], isLoading: false });
        }
      },

      // Fetch recent items (최근 접속한 채팅방/캐시글)
      fetchRecentItems: async () => {
        try {
          // TODO: 실제 API 연동
          const mockData: RecentItem[] = [
            {
              id: 1,
              icon: "🌅",
              title: "한강 선셋 피크닉",
              time: "2시간 전",
              type: "chat",
            },
            {
              id: 2,
              icon: "🏃",
              title: "주말 등산 모임",
              time: "어제",
              type: "chat",
            },
            {
              id: 3,
              icon: "📚",
              title: "독서 토론회",
              time: "3일 전",
              type: "meeting",
            },
            {
              id: 4,
              icon: "🎨",
              title: "수채화 그리기",
              time: "1주일 전",
              type: "meeting",
            },
          ];
          set({ recentItems: mockData });
        } catch (error) {
          console.error("❌ 최근 항목 조회 실패:", error);
        }
      },

      fetchAIRecommendation: async (userId: number) => {
        try {
          console.log("🎯 AI 추천 요청 시작:", userId);

          const response = await axios.get(
            `${API_BASE_URL}/ai/recommendations/personalized/${userId}`
          );

          console.log("📥 AI 추천 원본 응답:", response.data);

          // ⭐ 응답 검증
          if (!response.data) {
            console.error("❌ 응답 데이터 없음");
            set({ aiRecommendation: null });
            return;
          }

          if (!response.data.success) {
            console.warn("⚠️ 추천 실패:", response.data.message);
            set({ aiRecommendation: null });
            return;
          }

          // ⭐ meetingId 확인
          if (!response.data.meetingId) {
            console.error("❌ meetingId 없음:", response.data);
            set({ aiRecommendation: null });
            return;
          }

          // ⭐ Meeting 객체 생성
          const meeting: Meeting = {
            meetingId: response.data.meetingId,
            title: response.data.title || "제목 없음",
            description: response.data.description || "",
            category: response.data.category || "",
            subcategory: response.data.subcategory || "",
            locationName:
              response.data.locationName ||
              response.data.location ||
              "위치 미정",
            meetingTime: response.data.meetingTime || new Date().toISOString(),
            maxParticipants: response.data.maxParticipants || 0,
            currentParticipants: response.data.currentParticipants || 0,
            expectedCost: response.data.expectedCost || 0,
            vibe: response.data.vibe || "",
            imageUrl: response.data.imageUrl,
            avgRating: response.data.avgRating,
            organizerId: response.data.organizerId || 0,
          };

          console.log("✅ Meeting 객체 생성 완료:", meeting);
          set({ aiRecommendation: meeting });
        } catch (error) {
          console.error("❌ AI 추천 조회 실패:", error);
          if (axios.isAxiosError(error)) {
            console.error("응답 상태:", error.response?.status);
            console.error("응답 데이터:", error.response?.data);
          }
          set({ aiRecommendation: null });
        }
      },

      // Set category filter
      setCategory: (category: string) => {
        set({ selectedCategory: category });
      },

      // Set search query
      setSearchQuery: (query: string) => {
        set({ searchQuery: query });
      },

      // Search meetings
      searchMeetings: async (query: string) => {
        set({ isLoading: true, searchQuery: query });
        try {
          const response = await axios.post(`${API_BASE_URL}/meetings/search`, {
            keyword: query,
            page: 0,
            size: 50,
          });

          const meetingsData = response.data.meetings || [];
          set({
            meetings: Array.isArray(meetingsData) ? meetingsData : [],
            isLoading: false,
          });
        } catch (error) {
          console.error("❌ 모임 검색 실패:", error);
          set({ meetings: [], isLoading: false });
        }
      },
    }),

    {
      name: "meeting-storage", // localStorage key
      partialize: (state) => ({
        recentItems: state.recentItems,
        selectedCategory: state.selectedCategory,
      }),
    }
  )
);
