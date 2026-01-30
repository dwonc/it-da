import React, { useState, useEffect, useRef, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useChatStore, type ChatMessage } from "@/stores/useChatStore.ts";
import { chatApi } from "@/api/chat.api.ts";
import ChatMessageItem from "../../components/chat/ChatMessage";
import ChatMemberList from "../../components/chat/ChatMemberList";
import { useAuthStore } from "@/stores/useAuthStore";
import toast from "react-hot-toast";
import ChatReportModal from "./ChatReportModal";
import { User } from "@/types/user.types.ts";
import "./ChatRoomPage.css";
import BillInputModal from "../../components/chat/BillInputModal";
import PollInputModal from "../../components/chat/PollInputModal";
import api from "@/api/axios.config";
import InviteMemberModal from "@/components/chat/InviteMemberModal.tsx";
import NotificationBell from "@/components/chat/NotificationBell.tsx";

// ... (Interface 정의는 동일하게 유지)
interface BillData {
  totalAmount: number;
  participantCount: number;
  account: string;
}

interface PollData {
  title: string;
  options: string[];
  isAnonymous?: boolean;
  isMultipleChoice?: boolean;
}

interface RawMemberResponse {
  userId: number;
  username: string;
  nickname?: string;
  email: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
  profileImageUrl?: string;
  role?: string;
  isFollowing: boolean;
}
interface RecommendedPlace {
    placeName: string;
    address: string;
    latitude: number;
    longitude: number;
    distanceKm?: number;
}

const ChatRoomPage: React.FC = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const {
    messages,
    addMessage,
    setMessages,
    markAllAsRead,
    decrementUnreadCount,
  } = useChatStore();
  const [members, setMembers] = useState<User[]>([]);
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);
  const { user: currentUser } = useAuthStore();
  const currentUserMemberInfo = useMemo(
    () => members.find((m) => m.userId === currentUser?.userId),
    [members, currentUser],
  );
  // 백엔드에서 ORGANIZER로 내려주는 값을 프론트에서 LEADER로 매핑 중이므로 아래와 같이 설정합니다.
  const isLeader = currentUserMemberInfo?.role === "LEADER";
  const isOrganizer = isLeader; // 방장에게 공지 권한 부여
  const [reportTarget, setReportTarget] = useState<{
    id: number;
    name: string;
  } | null>(null);
  const [activeModal, setActiveModal] = useState<"BILL" | "POLL" | null>(null);
  const [roomTitle, setRoomTitle] = useState<string>("채팅방");

  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const messageEndRef = useRef<HTMLDivElement>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [roomMembers, setRoomMembers] = useState<
    { userId: number; nickname: string }[]
  >([]);
  const [inputValue, setInputValue] = useState<string>("");
  const navigate = useNavigate();
  const [linkedMeetingId, setLinkedMeetingId] = useState<number | null>(null);

  const [notice, setNotice] = useState<string>("");
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [recommendedPlaces, setRecommendedPlaces] = useState<RecommendedPlace[]>([]);
  const [isPlaceModalOpen, setIsPlaceModalOpen] = useState(false);
  const [selectedMapPlace, setSelectedMapPlace] = useState<any>(null);
  const [isMapModalOpen, setIsMapModalOpen] = useState(false);
  const isMounted = useRef(false);
  const [isChoiceModalOpen, setIsChoiceModalOpen] = useState(false);

    useEffect(() => {
        if (roomId) {
            const savedPlaces = localStorage.getItem(`rec_places_${roomId}`);
            if (savedPlaces) {
                try {
                    setRecommendedPlaces(JSON.parse(savedPlaces));
                } catch (e) {
                    console.error("저장된 장소 로드 실패:", e);
                    localStorage.removeItem(`rec_places_${roomId}`); // 오염된 데이터 삭제
                }
            }
        }
    }, [roomId]);

    const sendLocationMessage = async (place: any) => {
        if (!roomId || !currentUser || !linkedMeetingId) {
            toast.error("모임 정보가 연결되지 않았습니다.");
            return;
        }

        try {
            // ✅ 1. 백엔드 모임 테이블 업데이트 (API 경로는 프로젝트 구조에 맞게 수정 필요)
            await api.patch(`/meetings/${linkedMeetingId}/location`, {
                locationName: place.placeName,
                locationAddress: place.address,
                latitude: place.latitude,
                longitude: place.longitude
            });

            // 2. 채팅방에 확정 메시지 전송
            chatApi.sendMessage(
                Number(roomId),
                currentUser.email,
                currentUser.userId,
                `📍 확정된 모임 장소: ${place.placeName}\n주소: ${place.address}`,
                "LOCATION",
                {
                    placeName: place.placeName,
                    address: place.address,
                    lat: place.latitude,
                    lng: place.longitude,
                    isAiRecommendation: true
                }
            );

            setIsPlaceModalOpen(false);
            toast.success("모임 장소가 확정 및 업데이트되었습니다!");
        } catch (error) {
            console.error("장소 업데이트 실패:", error);
            toast.error("장소 정보 저장 중 오류가 발생했습니다.");
        }
    };

    const handleAIButtonClick = () => {
        // 이전에 저장된 추천 목록이 있다면 -> 선택 모달 띄우기
        if (recommendedPlaces.length > 0) {
            setIsPlaceModalOpen(false);
            setIsChoiceModalOpen(true);
        } else {
            // 없으면 -> 바로 AI API 호출
            fetchNewRecommendations();
        }
    };

  // 1️⃣ [수정됨] showAIRecommendation 함수를 return 문 밖(컴포넌트 로직 부분)으로 이동
  const fetchNewRecommendations = async () => {
      setIsChoiceModalOpen(false);
      setIsPlaceModalOpen(false);

      try {
          toast.loading("🤖 AI가 최적의 장소를 분석 중입니다...", { id: "ai-loading" });

          const response = await api.post("/ai/recommendations/recommend-place", {
              chatRoomId: Number(roomId),
          });

          toast.dismiss("ai-loading");

          if (response.data.success && response.data.recommendations?.length > 0) {
              const places = response.data.recommendations;

              // 1️⃣ 서버 DB에 저장될 상세 메타데이터 구성 (새로고침 시 스타일 유지의 핵심)
              const recommendationMetadata = {
                  isAiRecommendation: true,
                  centroid: response.data.centroid,
                  recommendations: places.map((p: any) => ({
                      placeName: p.placeName || p.name,
                      address: p.address,
                      latitude: p.latitude,
                      longitude: p.longitude,
                      distanceKm: p.distanceKm || p.distanceFromCentroid,
                      matchReasons: p.matchReasons
                  }))
              };

              // 2️⃣ 채팅방에 표시될 텍스트 내용 구성
              const chatMessageContent =
                  `🤖 AI가 최적의 장소를 추천해드립니다!\n\n` +
                  `📍 중간 지점: ${response.data.centroid?.address || "계산 완료"}\n\n` +
                  places.map((p: any, idx: number) =>
                      `${idx + 1}. ${p.placeName || p.name} ⭐\n` +
                      `📍 ${p.address}\n` +
                      `🏃 중간지점에서 ${p.distanceKm?.toFixed(1) || p.distanceFromCentroid?.toFixed(1) || 0}km\n` +
                      `💡 ${p.matchReasons?.join(", ") || "접근성이 좋은 장소예요"}`
                  ).join("\n\n");

              // 3️⃣ 서버로 메시지 전송 (metadata를 반드시 포함해야 새로고침 시 유지됨)
              chatApi.sendMessage(
                  Number(roomId),
                  currentUser!.email,
                  currentUser!.userId,
                  chatMessageContent,
                  "TALK",
                  {
                      isAiRecommendation: true,
                      ...recommendationMetadata,
                      centroid: response.data.centroid
                  }
              );

              // 4️⃣ 로컬 상태 업데이트
              setRecommendedPlaces(recommendationMetadata.recommendations);
              localStorage.setItem(`rec_places_${roomId}`, JSON.stringify(recommendationMetadata.recommendations));
              setIsPlaceModalOpen(true);

              toast.success("AI 추천이 완료되었습니다!");
          } else {
              toast.error(response.data.message || "추천 가능한 장소가 없습니다.");
          }
      } catch (error: any) {
          console.error("AI 추천 실패:", error);
          const errorMsg = error.response?.status === 500
              ? "주변에 적절한 장소가 없거나 위치 정보가 부족합니다."
              : "장소 추천을 불러올 수 없습니다.";
          toast.error(errorMsg);
          toast.dismiss("ai-loading");
      }
  };

  const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !roomId) return;

    try {
      setIsLoading(true);
      await chatApi.uploadImage(Number(roomId), file);
      toast.success("이미지를 전송했습니다.");
    } catch (error) {
      console.error("이미지 전송 실패:", error);
      toast.error("이미지 전송에 실패했습니다.");
    } finally {
      setIsLoading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleScroll = async () => {
    if (!chatContainerRef.current || !hasMore || isLoading) return;

    if (chatContainerRef.current.scrollTop === 0) {
      setIsLoading(true);
      const previousHeight = chatContainerRef.current.scrollHeight;

      try {
        const oldMessages = await chatApi.getChatMessages(
          Number(roomId),
          page + 1,
          50,
        );

        if (oldMessages && oldMessages.length > 0) {
          const validatedOldMessages: ChatMessage[] = (
            oldMessages as any[]
          ).map((msg) => ({
            ...msg,
            senderNickname: msg.senderNickname || "사용자",
            unreadCount: Number(msg.unreadCount ?? 0),
            sentAt: msg.sentAt || new Date().toISOString(),
          }));

          const combined = [...validatedOldMessages, ...messages];
          const uniqueMap = new Map();
          combined.forEach((msg) => {
            if (!uniqueMap.has(msg.messageId)) {
              uniqueMap.set(msg.messageId, msg);
            }
          });

          const uniqueSorted = Array.from(uniqueMap.values()).sort(
            (a, b) =>
              new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime(),
          );

          setMessages(uniqueSorted);
          setPage((prev) => prev + 1);

          setTimeout(() => {
            if (chatContainerRef.current) {
              chatContainerRef.current.scrollTop =
                chatContainerRef.current.scrollHeight - previousHeight;
            }
          }, 50);
        } else {
          setHasMore(false);
        }
      } catch (e) {
        console.error("과거 기록 로드 실패:", e);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleFeatureSubmit = async (type: "BILL" | "POLL", data: any) => {
    if (!roomId || !currentUser?.email || !currentUser?.userId) {
      toast.error("로그인 세션이 만료되었습니다.");
      return;
    }

    try {
      if (type === "BILL") {
        const perPerson = Math.floor(data.totalAmount / data.participantCount);
        const updatedParticipants = data.participants.map((p: any) => ({
          ...p,
          isPaid: Number(p.userId) === Number(currentUser?.userId),
        }));
        const content = `💰 정산 요청: 1인당 ${perPerson.toLocaleString()}원`;
        const metadata = {
          ...data,
          participants: updatedParticipants,
          amountPerPerson: perPerson,
        };

        chatApi.sendMessage(
          Number(roomId),
          currentUser.email,
          currentUser.userId,
          content,
          type,
          metadata,
        );
      } else if (type === "POLL") {
        await api.post(
          `/votes/${roomId}`,
          {
            title: data.title,
            isAnonymous: data.isAnonymous || false,
            isMultipleChoice: data.isMultipleChoice || false,
            options: data.options,
          },
          { withCredentials: true },
        );
        console.log("✅ 투표 생성 API 호출 완료");
      }

      setActiveModal(null);
      toast.success("메시지를 전송했습니다.");
    } catch (error) {
      console.error("❌ 전송 실패:", error);
      toast.error("전송에 실패했습니다.");
    }
  };

  useEffect(() => {
    const initChat = async () => {
      if (!roomId || !currentUser) return;
      console.log("🔍 [ChatRoom] Initializing with RoomID:", roomId);


        if (!isMounted.current) {
            try {
                await chatApi.markAsRead(Number(roomId));
                markAllAsRead();
            } catch (e) {
                console.warn("⚠️ 읽음 처리 실패:", e);
            }
            isMounted.current = true;
        }

      try {
        try {
          const history = await chatApi.getChatMessages(Number(roomId), 0, 50);
          const validatedHistory: ChatMessage[] = (history as any[]).map(
            (msg) => ({
              ...msg,
              senderNickname: msg.senderNickname || "사용자",
              content: msg.content || "",
              unreadCount: Number(msg.unreadCount ?? 0),
              sentAt: msg.sentAt || new Date().toISOString(),
              type: msg.type as ChatMessage["type"],
              metadata: msg.metadata || null,
            }),
          );
          setMessages(validatedHistory);
        } catch (e) {
          console.error("❌ 메시지 로드 실패:", e);
        }

          try {
              const rooms = await chatApi.getRooms();
              console.log("🔍 전체 응답:", JSON.stringify(rooms, null, 2));

              const currentRoom = rooms.find((r: any) => r.chatRoomId === Number(roomId));
              console.log("✅ currentRoom:", currentRoom);
              console.log("✅ meetingId:", currentRoom?.meetingId);

            if (currentRoom) {
                setRoomTitle(currentRoom.roomName);
                setNotice(currentRoom.notice || "");

                // ✅ 서버 응답 필드명이 meetingId인지, 혹은 객체 형태인지 확인하여 할당
                const mId = currentRoom.meetingId || currentRoom.meeting?.id;
                if (mId) {
                    setLinkedMeetingId(Number(mId));
                    console.log("🔗 연결된 모임 ID 설정 완료:", mId);
                } else {
                    console.warn("⚠️ 이 채팅방에 연결된 모임 ID가 없습니다.");
                }
            }
        } catch (e) {
            console.warn("⚠️ 방 정보 로드 실패", e);
        }

        try {
          const rawMembers: RawMemberResponse[] = await chatApi.getRoomMembers(
            Number(roomId),
          );
          const formattedMembers: User[] = rawMembers.map(
            (m: RawMemberResponse) => ({
              id: m.userId,
              userId: m.userId,
              name: m.nickname?.trim() ? m.nickname : m.username,
              username: m.username,
              nickname: m.nickname,
              email: m.email,
              status: (m.status || "ACTIVE") as User["status"],
              createdAt: m.createdAt || new Date().toISOString(),
              updatedAt: m.updatedAt || new Date().toISOString(),
              profileImageUrl: m.profileImageUrl || "",
              role: m.role === "ORGANIZER" ? "LEADER" : "MEMBER",
              isFollowing: m.isFollowing,
            }),
          );
          setMembers(formattedMembers);
          setRoomMembers(
            rawMembers.map((m) => ({
              userId: m.userId,
              nickname: m.nickname?.trim() ? m.nickname : m.username,
            })),
          );
        } catch (e) {
          console.error("❌ 멤버 로드 실패:", e);
          setMembers([]);
        }
      } catch (e) {
        console.error("🚨 예상치 못한 치명적 오류:", e);
      }
        await fetchRoomMembers();
    };

    initChat();

    let isSubscribed = true;

    if (roomId && currentUser?.email) {
      chatApi.disconnect();

      chatApi.connect(
        Number(roomId),
        currentUser.email,
        (newMsg: any) => {
          if (!isSubscribed) return;

            if (newMsg.type === "READ") {
                console.log("📖 읽음 신호 수신:", newMsg);

                // 내가 읽은 게 아닐 때만 숫자 감소
                if (currentUser && newMsg.email !== currentUser.email) {
                    // 전역 배지 감소 (선택사항)
                    decrementUnreadCount();

                    // 현재 화면 메시지들의 카운트 갱신
                    const currentMessages = useChatStore.getState().messages;
                    const updatedMessages = currentMessages.map((msg) => ({
                        ...msg,
                        // 0보다 클 때만 -1
                        unreadCount: msg.unreadCount > 0 ? msg.unreadCount - 1 : 0,
                    }));
                    setMessages(updatedMessages);
                }
                return; // 중요: 아래의 addMessage가 실행되지 않도록 리턴
            }

          if (newMsg.type === "BILL_UPDATE") {
            const targetId = Number(
              newMsg.targetMessageId || newMsg.metadata.messageId,
            );
            addMessage({
              ...newMsg,
              messageId: targetId,
              type: "BILL",
              metadata:
                typeof newMsg.metadata === "string"
                  ? JSON.parse(newMsg.metadata)
                  : newMsg.metadata,
            });
            return;
          }
          if (newMsg.type === "NOTICE") {
              setTimeout(() => {
                  fetchRoomMembers();
              }, 500);
          }

          if (newMsg.type === "TALK" || newMsg.type === "IMAGE" || newMsg.type === "LOCATION" || newMsg.type === "VOTE") {
              if (currentUser && Number(newMsg.senderId) !== Number(currentUser.userId)) {
                    // 약간의 딜레이를 주어 UI가 먼저 그려지게 해도 좋고, 바로 호출해도 됩니다.
                    chatApi.markAsRead(Number(roomId));
              }
          }

          const serverCount = Number(newMsg.unreadCount ?? 0);

          const validatedMsg: ChatMessage = {
            ...newMsg,
            unreadCount: serverCount,
            senderNickname: newMsg.senderNickname || "사용자",
            sentAt: newMsg.sentAt || new Date().toISOString(),
            senderId: Number(newMsg.senderId),
            messageId: Number(newMsg.messageId) || Date.now(),
            metadata:
              typeof newMsg.metadata === "string"
                ? JSON.parse(newMsg.metadata)
                : newMsg.metadata,
          };

          addMessage(validatedMsg);
        }
      );
    }
    return () => {
      isSubscribed = false;
      chatApi.disconnect();
    };
  }, [roomId, currentUser, setMessages, markAllAsRead, decrementUnreadCount]);

    const fetchRoomMembers = async () => {
        if (!roomId) return;
        try {
            const rawMembers = await chatApi.getRoomMembers(Number(roomId));
            const formattedMembers: User[] = rawMembers.map((m: any) => ({
                id: m.userId,
                userId: m.userId,
                name: m.nickname?.trim() ? m.nickname : m.username,
                username: m.username,
                nickname: m.nickname,
                email: m.email,
                status: m.status || "ACTIVE",
                profileImageUrl: m.profileImageUrl || "",
                role: m.role === "ORGANIZER" ? "LEADER" : "MEMBER",
                isFollowing: m.isFollowing,
            }));
            setMembers(formattedMembers);
            console.log("🔄 멤버 목록 갱신 완료:", formattedMembers.length, "명");
        } catch (e) {
            console.error("멤버 목록 갱신 실패:", e);
        }
    };

  const handleEditMeeting = () => {
    if (!linkedMeetingId) {
      toast.error("연결된 모임 정보를 찾을 수 없습니다.");
      return;
    }
    navigate(`/meetings/${linkedMeetingId}/edit`);
  };

  const handleMeetingDetail = () => {
    if (!linkedMeetingId) {
      toast.error("연결된 모임 정보를 찾을 수 없습니다.");
      return;
    }
    navigate(`/meetings/${linkedMeetingId}`);
  };

  const handleSendMessage = () => {
    if (
      !roomId ||
      !currentUser?.email ||
      !currentUser?.userId ||
      !inputValue.trim()
    ) {
      if (!inputValue.trim()) return;
      toast.error("로그인 세션이 만료되었습니다.");
      return;
    }
    chatApi.sendMessage(
      Number(roomId),
      currentUser.email,
      currentUser.userId,
      inputValue,
      "TALK",
      {},
    );

    setInputValue("");
  };

  const handleFeatureAction = (feature: string) => {
    if (!roomId || !currentUser?.email) return;

    switch (feature) {
      case "📷":
        fileInputRef.current?.click();
        break;
      case "📊":
        setActiveModal("POLL");
        break;
      case "💰":
        setActiveModal("BILL");
        break;
        case "📍":
            if (recommendedPlaces.length === 0) {
                toast.error("먼저 'AI 추천 받기'를 눌러 장소를 분석해 주세요!");
                return;
            }
            // ✅ prompt 대신 우리가 만든 이쁜 모달을 띄웁니다.
            setIsPlaceModalOpen(true);
            break;
    }
  };

  const handleFollow = async (targetUserId: number) => {
    try {
      await chatApi.followUser(targetUserId);
      toast.success("팔로우가 완료되었습니다!");
      setMembers((prev) =>
        prev.map((m) =>
          m.userId === targetUserId ? { ...m, isFollowing: true } : m,
        ),
      );
    } catch (error) {
      console.error("팔로우 실패:", error);
      toast.error("팔로우 처리 중 오류가 발생했습니다.");
    }
  };

  const handleReportSubmit = async (reason: string) => {
    if (!reportTarget) return;
      try {
          // ✅ chatApi를 사용하여 서버에 신고 접수
          await chatApi.reportUser(reportTarget.id, reason);
          toast.success("신고가 정상적으로 접수되었습니다.");
          setReportTarget(null);
      } catch (error) {
          toast.error("신고 접수 중 오류가 발생했습니다.");
          console.error("신고 실패:", error);
      }
  };

  const scrollToBottom = () => {
    if (page === 0) {
      messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const renderMessages = () => {
    let lastDateLabel = "";
    const uniqueMessages = messages.reduce(
      (acc, msg) => {
        if (!acc.find((m) => m.messageId === msg.messageId)) {
          acc.push(msg);
        }
        return acc;
      },
      [] as typeof messages,
    );
    return uniqueMessages.map((msg, idx) => {
      const msgDate = msg.sentAt ? new Date(msg.sentAt) : new Date();
      if (isNaN(msgDate.getTime())) return null;

      const dateLabel = msgDate.toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
      });

      const showDateDivider = lastDateLabel !== dateLabel;
      lastDateLabel = dateLabel;

      const isMine = Number(msg.senderId) === Number(currentUser?.userId);

      return (
        <React.Fragment key={`msg-${msg.messageId}-${idx}`}>
          {showDateDivider && (
            <div className="date-divider" key={`date-${dateLabel}`}>
              <span>{dateLabel}</span>
            </div>
          )}
          <div className={`message-row ${isMine ? "mine" : "others"}`}>
            <ChatMessageItem message={msg} isMine={isMine} onLocationClick={handleLocationClick} />
          </div>
        </React.Fragment>
      );
    });
  };
    useEffect(() => {
        // 🚨 maps 객체와 생성자가 존재하는지 엄격히 체크
        if (isMapModalOpen && selectedMapPlace && window.kakao && window.kakao.maps) {

            // 카카오 지도 라이브러리가 완전히 로드될 때까지 대기
            window.kakao.maps.load(() => {
                const container = document.getElementById('kakao-map');
                if (!container) return;

                try {
                    const options = {
                        center: new window.kakao.maps.LatLng(selectedMapPlace.lat, selectedMapPlace.lng),
                        level: 3
                    };

                    const map = new window.kakao.maps.Map(container, options);

                    // 마커 표시
                    const markerPosition = new window.kakao.maps.LatLng(selectedMapPlace.lat, selectedMapPlace.lng);
                    const marker = new window.kakao.maps.Marker({ position: markerPosition });
                    marker.setMap(map);

                    // 인포윈도우(장소명) 표시
                    const iwContent = `<div style="padding:5px; font-size:12px; text-align:center;">${selectedMapPlace.placeName}</div>`;
                    const infowindow = new window.kakao.maps.InfoWindow({ content: iwContent });
                    infowindow.open(map, marker);
                } catch (error) {
                    console.error("지도 생성 중 오류 발생:", error);
                }
            });
        }
    }, [isMapModalOpen, selectedMapPlace]);

// 3. 메시지 클릭 핸들러 (LOCATION 타입 메시지용)
    const handleLocationClick = (metadata: any) => {
        setSelectedMapPlace(metadata);
        setIsMapModalOpen(true);
    };
  const handleEditNotice = async () => {
    if (!isOrganizer) {
      toast.error("방장만 공지사항을 수정할 수 있습니다.");
      return;
    }
    const newNotice = prompt("새로운 공지사항을 입력하세요:", notice);
    if (newNotice === null) return;

    try {
      await chatApi.updateNotice(Number(roomId), newNotice);
      setNotice(newNotice);
      toast.success("공지사항이 등록되었습니다.");
    } catch (error) {
      console.error("공지 수정 실패:", error);
      toast.error("공지사항 등록에 실패했습니다.");
    }
  };

  return (
    <div className="chat-room-container">
      <header className="header">
        <div className="header-content">
          <button
            className="back-btn"
            onClick={() => navigate("/")}
            style={{ cursor: "pointer" }}
          >
            ←
          </button>
          <div className="header-info">
            <div className="room-title">🌅 {roomTitle}</div>
            <div className="room-meta">{members.length}명 참여중</div>
          </div>
          <div className="header-actions">
              <div className="chat-notification-wrapper">
                  <NotificationBell />
              </div>
            <button className="icon-btn" onClick={() => setIsMenuOpen(true)}>
              ☰
            </button>
          </div>
        </div>
      </header>

      {/* ✅ 공지사항 배너 */}
      {notice && (
        <div className="notice-banner">
          <span className="notice-icon">📢</span>
          <span className="notice-text">{notice}</span>
        </div>
      )}

      {/* 2️⃣ [수정됨] 함수 호출을 위한 UI 배너 추가 (그라데이션 디자인) */}
      <div
        className="ai-recommendation-banner"
        style={{
            background: "linear-gradient(90deg, #6a11cb 0%, #2575fc 100%)",
            color: "white",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            boxShadow: "0 4px 6px rgba(0,0,0,0.1)",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <span style={{ fontSize: "1.2rem" }}>🤖</span>
          <span style={{ fontSize: "0.9rem", fontWeight: 600 }}>
            어디서 만날지 고민되시나요?
          </span>
        </div>
        <button
          onClick={handleAIButtonClick}
          style={{
            backgroundColor: "rgba(255,255,255,0.2)",
            border: "1px solid rgba(255,255,255,0.4)",
            color: "white",
            padding: "6px 12px",
            borderRadius: "20px",
            cursor: "pointer",
            fontSize: "0.85rem",
            fontWeight: "bold",
            backdropFilter: "blur(4px)",
          }}
        >
          AI 추천 받기
        </button>
      </div>

      <main
        className="chat-container"
        ref={chatContainerRef}
        onScroll={handleScroll}
        style={{ paddingBottom: "80px" }}
      >
        {isLoading && (
          <div className="loading-spinner">과거 메시지 로드 중...</div>
        )}
        {renderMessages()}
        <div ref={messageEndRef} />
      </main>

      <footer className="input-area">
        <div className="quick-actions">
          <input
            type="file"
            ref={fileInputRef}
            style={{ display: "none" }}
            accept="image/*"
            onChange={handleImageSelect}
          />
          <button
            className="quick-btn"
            onClick={() => handleFeatureAction("📷")}
          >
            📷
          </button>
          <button className="quick-btn" onClick={() => setActiveModal("POLL")}>
            📊
          </button>
          <button
            className="quick-btn"
            onClick={() => handleFeatureAction("📍")}
          >
            📍
          </button>
          <button className="quick-btn" onClick={() => setActiveModal("BILL")}>
            💰
          </button>
        </div>
        <input
          className="message-input"
          placeholder="메시지를 입력하세요..."
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyPress={(e) => {
            if (e.key === "Enter") {
              e.preventDefault();
              handleSendMessage();
            }
          }}
        />
        <button
          className="send-btn"
          onClick={handleSendMessage}
          style={{
            cursor: inputValue.trim() ? "pointer" : "default",
            opacity: inputValue.trim() ? 1 : 0.6,
          }}
        >
          ➤
        </button>
      </footer>
        {isPlaceModalOpen && (
            <div className="modal-overlay" onClick={() => setIsPlaceModalOpen(false)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "15px" }}>
                        <h3 style={{ margin: 0 }}>📍 추천 장소 선택</h3>
                        <button
                            onClick={() => setIsPlaceModalOpen(false)}
                            style={{ background: "none", border: "none", fontSize: "1.5rem", cursor: "pointer", color: "#999" }}
                        >
                            ×
                        </button>
                    </div>

                    <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: "15px" }}>
                        채팅방에 공유할 장소를 선택해주세요.
                    </p>

                    <div className="place-list" style={{ maxHeight: "350px", overflowY: "auto", display: "flex", flexDirection: "column", gap: "10px" }}>
                        {recommendedPlaces.map((place, idx) => (
                            <div
                                key={idx}
                                className="place-item"
                                onClick={() => sendLocationMessage(place)} // 👈 여기서 sendLocationMessage 사용
                                style={{
                                    padding: "15px",
                                    border: "1px solid #eee",
                                    borderRadius: "12px",
                                    cursor: "pointer",
                                    transition: "all 0.2s",
                                    textAlign: "left"
                                }}
                            >
                                <div style={{ fontWeight: "bold", color: "#333", fontSize: "1rem", marginBottom: "4px" }}>
                                    {place.placeName}
                                </div>
                                <div style={{ fontSize: "0.8rem", color: "#888", lineHeight: "1.4" }}>
                                    {place.address}
                                </div>
                            </div>
                        ))}
                    </div>

                    <button
                        className="submit-btn"
                        style={{ background: "#f1f3f5", color: "#495057", marginTop: "20px", width: "100%" }}
                        onClick={() => setIsPlaceModalOpen(false)}
                    >
                        닫기
                    </button>
                </div>
            </div>
        )}

      {/* ✅ 정산 입력 모달 */}
      {activeModal === "BILL" && (
        <BillInputModal
          onClose={() => setActiveModal(null)}
          onSubmit={(data: BillData) => handleFeatureSubmit("BILL", data)}
          members={roomMembers || []}
        />
      )}

      {/* ✅ 투표 입력 모달 */}
      {activeModal === "POLL" && (
        <PollInputModal
          onClose={() => setActiveModal(null)}
          onSubmit={(data: PollData) => handleFeatureSubmit("POLL", data)}
        />
      )}
      {isMenuOpen && (
        <>
          <div
            className="overlay active"
            onClick={() => setIsMenuOpen(false)}
          ></div>
          <div className="side-menu active">
            <div className="menu-header">
              <div className="menu-title">모임 정보</div>
              <button
                className="close-btn"
                onClick={() => setIsMenuOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="menu-section">
              <div className="section-title">
                참여 멤버 ({members.length}명)
              </div>
              {/* ✅ ChatMemberList에 handleFollow 연결하여 ESLint 해결 */}
              <ChatMemberList
                members={members}
                onFollow={handleFollow}
                onReport={(id, name) => setReportTarget({ id, name })}
              />
            </div>
            <div className="menu-section admin-actions">
              {/* 3. 권한별 버튼 렌더링 조건부 처리 */}
              {isLeader && (
                <button className="menu-btn" onClick={handleEditMeeting}>
                  <span className="icon">⚙️</span> 모임 정보 수정
                </button>
              )}

              {/* 🚩 공지사항 수정: isOrganizer(LEADER)일 때만 노출 */}
              {isOrganizer && (
                <button className="menu-btn" onClick={handleEditNotice}>
                  <span className="icon">📢</span> 공지사항 수정
                </button>
              )}

              <button className="menu-btn" onClick={handleMeetingDetail}>
                <span className="icon">📄</span> 모임 상세보기
              </button>

              <button
                className="menu-btn"
                onClick={() => setIsInviteModalOpen(true)}
              >
                <span className="icon">➕</span> 멤버 초대
              </button>
              {isInviteModalOpen && (
                <InviteMemberModal
                  roomId={Number(roomId)}
                  onClose={() => setIsInviteModalOpen(false)}
                  onInviteCompleted={() => {
                    window.location.reload();
                  }}
                />
              )}
            </div>

            <div className="menu-section">
              <button
                className="menu-btn danger"
                onClick={() => {
                  if (confirm("방을 나가시겠습니까?")) window.history.back();
                }}
              >
                🚪 톡방 나가기
              </button>
            </div>
          </div>
        </>
      )}

      {reportTarget && (
        <ChatReportModal
          targetName={reportTarget.name}
          onClose={() => setReportTarget(null)}
          onSubmit={handleReportSubmit}
        />
      )}
        {isChoiceModalOpen && (
            <div className="modal-overlay" onClick={() => setIsChoiceModalOpen(false)}>
                <div className="modal-content" style={{ maxWidth: '350px', textAlign: 'center' }} onClick={(e) => e.stopPropagation()}>
                    <h3 style={{ marginBottom: '15px' }}>🤖 AI 장소 추천</h3>
                    <p style={{ color: '#666', marginBottom: '25px' }}>
                        이전에 추천받은 장소 목록이 있습니다.<br />
                        어떻게 하시겠습니까?
                    </p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        <button
                            onClick={() => {
                                setIsChoiceModalOpen(false);
                                setIsPlaceModalOpen(true); // 이전 목록 모달 열기
                                toast.success("이전 추천 리스트를 불러왔습니다.");
                            }}
                            style={{
                                padding: '12px',
                                borderRadius: '8px',
                                border: '1px solid #ddd',
                                background: 'white',
                                cursor: 'pointer',
                                fontWeight: 'bold',
                                color: '#333'
                            }}
                        >
                            📂 이전 목록 보기
                        </button>

                        <button
                            onClick={fetchNewRecommendations} // 새로 API 호출
                            style={{
                                padding: '12px',
                                borderRadius: '8px',
                                border: 'none',
                                background: 'linear-gradient(90deg, #6a11cb 0%, #2575fc 100%)',
                                cursor: 'pointer',
                                fontWeight: 'bold',
                                color: 'white'
                            }}
                        >
                            ✨ 새로 추천 받기
                        </button>
                    </div>

                    <button
                        onClick={() => setIsChoiceModalOpen(false)}
                        style={{ marginTop: '15px', background: 'none', border: 'none', color: '#999', cursor: 'pointer', textDecoration: 'underline' }}
                    >
                        취소
                    </button>
                </div>
            </div>
        )}

        {/* ✅ 지도 상세 확인 모달 */}
        {isMapModalOpen && selectedMapPlace && (
            <div className="modal-overlay" onClick={() => setIsMapModalOpen(false)}>
                <div className="modal-content" style={{ maxWidth: '450px', width: '90%' }} onClick={(e) => e.stopPropagation()}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                        <h3 style={{ margin: 0 }}>📍 장소 상세 정보</h3>
                        <button onClick={() => setIsMapModalOpen(false)} style={{ background: 'none', border: 'none', fontSize: '1.2rem', cursor: 'pointer' }}>×</button>
                    </div>

                    {/* 지도 영역 */}
                    <div id="kakao-map" style={{ width: '100%', height: '300px', borderRadius: '12px', background: '#eee' }}></div>

                    <div style={{ marginTop: '15px', textAlign: 'left' }}>
                        <div style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>{selectedMapPlace.placeName}</div>
                        <div style={{ color: '#666', fontSize: '0.9rem', marginTop: '4px' }}>{selectedMapPlace.address}</div>
                    </div>

                    <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                        <button
                            className="submit-btn"
                            onClick={() => window.open(`https://map.kakao.com/link/to/${selectedMapPlace.placeName},${selectedMapPlace.lat},${selectedMapPlace.lng}`)}
                            style={{ flex: 1, background: '#fee500', color: '#3c1e1e', border: 'none' }}
                        >
                            카카오맵 길찾기
                        </button>
                        <button
                            className="submit-btn"
                            onClick={() => setIsMapModalOpen(false)}
                            style={{ flex: 0.5, background: '#f1f3f5', color: '#495057' }}
                        >
                            닫기
                        </button>
                    </div>
                </div>
            </div>
        )}
    </div>
  );
};

export default ChatRoomPage;
