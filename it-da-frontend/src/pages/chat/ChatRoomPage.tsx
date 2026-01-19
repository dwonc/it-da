import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { useChatStore } from "../../stores/useChatStore";
import { chatApi } from "../../api/chat.api"; // ChatMessage 타입 활용
import ChatInput from "../../components/chat/ChatInput";
import ChatMessage from "../../components/chat/ChatMessage";
import ChatMemberList from "../../components/chat/ChatMemberList";
import { useAuthStore } from "@/stores/useAuthStore";
import toast from "react-hot-toast";
import ChatReportModal from "./ChatReportModal";
import { User } from "../../types/user.types";
import "./ChatRoomPage.css";
import BillInputModal from "../../components/chat/BillInputModal";
import PollInputModal from "../../components/chat/PollInputModal";

interface BillData {
    totalAmount: number;
    account: string;
}

interface PollData {
    title: string;
    options: string[];
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
}

const ChatRoomPage: React.FC = () => {
    const { roomId } = useParams<{ roomId: string }>();
    const { messages, addMessage, setMessages, markAllAsRead,decrementUnreadCount } = useChatStore();
    const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);
    const { user: currentUser } = useAuthStore();

    const [members, setMembers] = useState<User[]>([]);
    const [reportTarget, setReportTarget] = useState<{ id: number; name: string } | null>(null);
    const [activeModal, setActiveModal] = useState<"BILL" | "POLL" | null>(null);

    const [roomTitle,setRoomTitle]=useState<string>("채팅방");

    // AI 추천 알림창 (HTML 기능 반영)
    const showAIRecommendation = () => {
        toast("🤖 AI가 최적의 장소를 추천해드립니다!\n\n1. 여의도 한강공원 ⭐\n2. 반포 달빛광장\n3. 뚝섬 장미광장", {
            duration: 4000,
            icon: '🤖',
        });
    };

    const handleFeatureSubmit = (type: "BILL" | "POLL", data: BillData | PollData) => {
        if (!roomId || !currentUser?.email) return;

        const content = type === "BILL" ? "💰 정산 요청이 도착했습니다." : `📊 투표: ${(data as PollData).title}`;

        chatApi.sendMessage(
            Number(roomId),
            currentUser.email,
            currentUser.userId,
            content,
            type,
            data as unknown as Record<string, unknown>
        );

        setActiveModal(null);
        toast.success("메시지를 전송했습니다.");
    };

    useEffect(() => {
        const initChat = async () => {
            if (!roomId || !currentUser) return;

            try {
                const history = await chatApi.getChatMessages(Number(roomId));

                const validatedHistory = history.map(msg => ({
                    ...msg,
                    // senderNickname이 없거나 "익"이면 username으로 대체
                    senderNickname: msg.senderNickname && msg.senderNickname !== "익"
                        ? msg.senderNickname
                        : "사용자",
                    unreadCount: 0 // 과거 내역은 일단 0으로 표시 (서버에서 계산된 값이 오면 그것 사용)
                }));

                setMessages(validatedHistory);

                // 서버에 읽음 신호 전송
                await chatApi.markAsRead(Number(roomId), currentUser.email);
                chatApi.sendReadEvent(Number(roomId), currentUser.email);
                // Zustand 스토어 상태 강제 동기화 (구독 중인 다른 메시지들도 0으로 처리)
                markAllAsRead();

                // 2. 방 제목 동적 세팅 추가
                const rooms = await chatApi.getRooms(); //
                const currentRoom = rooms.find((r: any) => r.chatRoomId === Number(roomId)); //
                if (currentRoom) {
                    setRoomTitle(currentRoom.roomName); //
                }

                const rawMembers: RawMemberResponse[] = await chatApi.getRoomMembers(Number(roomId));
                const formattedMembers: User[] = rawMembers.map((m: RawMemberResponse) => {
                    // ✅ 닉네임이 있으면 닉네임만, 없으면 유저네임만 사용하여 이름 중복 방지
                    const displayName = m.nickname && m.nickname.trim() !== "" ? m.nickname : m.username;

                    return {
                        id: m.userId,
                        userId: m.userId,
                        name: displayName, // UI에서는 이 name 필드 하나만 사용하도록 유도
                        username: m.username,
                        nickname:m.nickname,
                        email: m.email,
                        status: (m.status as User['status']) || ("ACTIVE" as User['status']),
                        createdAt: m.createdAt || new Date().toISOString(),
                        updatedAt: m.updatedAt || new Date().toISOString(),
                        profileImageUrl: m.profileImageUrl || "",
                        role: m.userId === currentUser.userId ? "ME" : m.role === "ORGANIZER" ? "LEADER" : "MEMBER"
                    };
                });
                setMembers(formattedMembers);
            } catch (e) {
                console.error("데이터 로드 실패:", e);
                toast.error("채팅방 정보를 불러오는데 실패했습니다.");
            }
        };

        initChat();

        let isSubscribed = true;

        if (roomId && currentUser?.email) {
            // 기존 연결이 있다면 명시적으로 해제하여 중복 구독을 막습니다.
            chatApi.disconnect();

            chatApi.connect(Number(roomId), currentUser.email, (newMsg: any) => {
                if (isSubscribed) {
                    const isMine = String(newMsg.senderId) === String(currentUser.userId);

                    const validatedMsg = {
                        ...newMsg,
                        // 💡 서버가 준 숫자(예: 2)를 그대로 저장 (내가 보낸 거라면 2가 뜹니다)
                        unreadCount: Number(newMsg.unreadCount),
                        sentAt: newMsg.sentAt || new Date().toISOString(),
                        senderNickname: newMsg.senderNickname,
                        senderId: Number(newMsg.senderId),
                        messageId: Number(newMsg.messageId) || Date.now()
                    };

                    addMessage(validatedMsg);

                    // ✅ 핵심 1: 내가 방을 보고 있는데 남의 메시지가 왔다면, 즉시 읽음 신호 발송
                    // 이 신호가 가야 상대방 화면의 '2'가 '1'로 줄어듭니다.
                    if (!isMine) {
                        chatApi.sendReadEvent(Number(roomId), currentUser.email);
                    }
                }
            }, (readData: any) => {
                // ✅ 핵심 2: 누군가 읽었다는 신호(READ 이벤트)가 오면 내 화면의 모든 숫자 1씩 차감
                if (readData.email !== currentUser?.email) {
                    decrementUnreadCount(); // Zustand 스토어의 1씩 줄이는 기능 호출
                }
            });
        }
        return () => {
            isSubscribed = false;
            chatApi.disconnect();
        };
    }, [roomId, currentUser, setMessages, markAllAsRead]);



    const handleSendMessage = (text: string) => {
        if (!roomId || !currentUser?.email || !currentUser?.userId) {
            toast.error("로그인 세션이 만료되었습니다.");
            return;
        }
        chatApi.sendMessage(Number(roomId), currentUser.email, currentUser.userId, text, "TALK");
    };

    const handleFeatureAction = (feature: string) => {
        if (!roomId || !currentUser?.email) return;

        switch (feature) {
            case "📊":
                setActiveModal("POLL");
                break;
            case "💰":
                setActiveModal("BILL");
                break;
            case "📍":
                chatApi.sendMessage(
                    Number(roomId),
                    currentUser.email,
                    currentUser.userId, // ✅ 인자 추가됨
                    "📍 모임 장소 확인하세요.",
                    "LOCATION",
                    { placeName: "여의도 한강공원", lat: 37.5271, lng: 126.9328 }
                );
                toast.success("장소 정보를 전송했습니다.");
                break;
            case "📷":
                toast.error("이미지 전송 기능은 준비 중입니다.");
                break;
        }
    };

    const handleFollow = async (targetUserId: number) => {
        try {
            await chatApi.followUser(targetUserId);
            toast.success("팔로우가 완료되었습니다!");
        } catch {
            toast.error("팔로우 처리 중 오류가 발생했습니다.");
        }
    };

    const handleReportSubmit = async (reason: string) => {
        if (!reportTarget) return;
        console.log(`${reportTarget.name}님 신고 접수: ${reason}`);
        toast.success("신고가 정상적으로 접수되었습니다.");
        setReportTarget(null);
    };
    const messageEndRef = React.useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const renderMessages = () => {
        let lastDateLabel = "";

        return messages.map((msg, idx) => {
            // ✅ 1. msgDate를 안전하게 생성 (sentAt이 없으면 현재 시간 사용)
            const msgDate = msg.sentAt ? new Date(msg.sentAt) : new Date();

            if (isNaN(msgDate.getTime())) return null;

            // ✅ 2. 위에서 만든 msgDate를 사용하여 dateLabel 생성 (currentDate 중복 선언 삭제)
            const dateLabel = msgDate.toLocaleDateString("ko-KR", {
                year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
            });

            const showDateDivider = lastDateLabel !== dateLabel;
            lastDateLabel = dateLabel;

            const isMine = Number(msg.senderId) === Number(currentUser?.userId);

            return (
                // messageId가 실시간 메시지에서 아직 부여되지 않았을 경우를 대비해 고유값 조합
                <React.Fragment key={`room-${roomId}-msg-${msg.messageId || 'temp'}-${idx}-${msg.sentAt}`}>
                    {showDateDivider && (
                        <div className="date-divider">...</div>
                    )}
                    <div className={`message-row ${isMine ? 'mine' : 'others'}`}>
                        <ChatMessage message={msg} isMine={isMine} />
                    </div>
                </React.Fragment>
            );
        });
    };



    return (
        <div className="chat-room-container">
            <header className="header">
                <div className="header-content">
                    <button className="back-btn" onClick={() => window.history.back()}>←</button>
                    <div className="header-info">
                        <div className="room-title">🌅 {roomTitle}</div>
                        <div className="room-meta">{members.length}명 참여중</div>
                    </div>
                    <div className="header-actions">
                        <button className="icon-btn">🔔</button>
                        <button className="icon-btn" onClick={() => setIsMenuOpen(true)}>☰</button>
                    </div>
                </div>
            </header>

            {/* ✅ 공지사항 배너 */}
            <div className="notice-banner">
                <span className="notice-icon">📢</span>
                <span className="notice-text">모임 D-2! 여의도 한강공원 물빛광장에서 만나요</span>
            </div>

            {/* ✅ AI 추천 배너 (그라데이션 디자인) */}
            <div className="ai-banner" onClick={showAIRecommendation}>
                <span style={{ fontSize: "2rem" }}>🤖</span>
                <div className="ai-banner-content">
                    <div className="ai-banner-title">AI 장소 추천</div>
                    <div className="ai-banner-subtitle">날씨와 분위기에 맞는 최적의 장소를 추천해드려요</div>
                </div>
                <span>→</span>
            </div>

            <main className="chat-container">
                <div className="message-list-area">
                    {renderMessages()}
                    <div ref={messageEndRef} />
                </div>
            </main>

            <footer className="input-area">
                <ChatInput onSend={handleSendMessage} onShowFeature={handleFeatureAction} />
            </footer>

            {/* ✅ 정산 입력 모달 */}
            {activeModal === "BILL" && (
                <BillInputModal
                    onClose={() => setActiveModal(null)}
                    onSubmit={(data: BillData) => handleFeatureSubmit("BILL", data)}
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
                    <div className="overlay active" onClick={() => setIsMenuOpen(false)}></div>
                    <div className="side-menu active">
                        <div className="menu-header">
                            <div className="menu-title">모임 정보</div>
                            <button className="back-btn" onClick={() => setIsMenuOpen(false)}>×</button>
                        </div>
                        <div className="menu-section">
                            <div className="section-title">참여 멤버 ({members.length}명)</div>
                            <ChatMemberList
                                members={members}
                                onFollow={handleFollow}
                                onReport={(id, name) => setReportTarget({ id, name })}
                            />
                        </div>
                        <div className="menu-section">
                            <button className="menu-btn danger" onClick={() => { if(confirm('방을 나가시겠습니까?')) window.history.back(); }}>🚪 톡방 나가기</button>
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
        </div>
    );
};

export default ChatRoomPage;