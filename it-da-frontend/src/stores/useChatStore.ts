import { create } from "zustand";

export interface ChatMessage { // ✅ 이 부분이 Page의 인터페이스와 일치해야 함
    messageId: number;
    senderId: number;
    senderNickname: string;
    content: string;
    type: string;
    sentAt: string;
    unreadCount:number;
    email?:string;
}

interface ChatState {
    messages: ChatMessage[];
    addMessage: (message: ChatMessage) => void;
    setMessages: (messages: ChatMessage[]) => void;
    markAllAsRead: () => void;
    decrementUnreadCount: () => void;
}

export const useChatStore = create<ChatState>((set) => ({
    messages: [],
    addMessage: (msg) => set((state) => {
        // 1. 중복 메시지 체크
        const isDuplicate = state.messages.some((m) => m.messageId === msg.messageId);
        if (isDuplicate) return state;

        // 2. 서버가 준 unreadCount를 숫자로 변환하여 저장
        const newMsg = {
            ...msg,
            unreadCount: Number(msg.unreadCount) || 0
        };

        console.log("🗃️ Store에 추가되는 메시지:", newMsg);
        return { messages: [...state.messages, msg] };
    }),
    setMessages: (msgs) => set({ messages: msgs }),

    // ✅ 읽음 신호를 받으면 모든 숫자를 0으로 초기화
    markAllAsRead: () => set((state) => ({
        messages: state.messages.map((msg) => ({
            ...msg,
            unreadCount: 0
        }))
    })),
    // markAllAsRead 대신 1:N을 지원하려면 숫자를 1씩 줄이는 액션이 유리합니다.
    decrementUnreadCount: () => set((state) => ({
        messages: state.messages.map((msg) => ({
            ...msg,
            unreadCount: Math.max(0, msg.unreadCount - 1)
        }))
    })),
}));

