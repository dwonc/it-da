import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export interface VoteOption {
  optionId: number;
  content: string;
  voteCount: number;
  voterIds?: number[];
}

export interface VoteData {
  voteId: number;
  title: string;
  isAnonymous: boolean;
  isMultipleChoice: boolean;
  creatorId: number;
  creatorNickname: string;
  options: VoteOption[];
}

export interface ChatMessage {
  messageId: number;
  senderId: number;
  senderNickname: string;
  content: string;
  type:
    | "TALK"
    | "BILL"
    | "POLL"
    | "IMAGE"
    | "LOCATION"
    | "NOTICE"
    | "VOTE_UPDATE"
    | "BILL_UPDATE"
    | "AI_RECOMMENDATION";
  sentAt: string;
  unreadCount: number;
  email?: string;
  voteData?: VoteData;
  metadata?: any;
  voteId?: number;
  targetMessageId?: number;
}

interface ChatState {
  messages: ChatMessage[];
  pendingUnread: Record<number, number>;

  addMessage: (message: ChatMessage) => void;
  setMessages: (messages: ChatMessage[]) => void;
  updateVote: (voteData: VoteData) => void;
  updateUnreadCount: (messageId: number, unreadCount: number) => void;
}

export const useChatStore = create<ChatState>()(
  immer((set) => ({
    messages: [],
    pendingUnread: {},

    addMessage: (msg) =>
      set((state) => {
        const voteIdFromMeta = msg.metadata?.voteId;
        const pending = state.pendingUnread[msg.messageId];
        const mergedMsg =
          pending !== undefined ? { ...msg, unreadCount: pending } : msg;

        // 1) 투표 업데이트
        if (mergedMsg.type === "VOTE_UPDATE" || voteIdFromMeta) {
          const targetVoteId = String(mergedMsg.voteId || voteIdFromMeta);
          const existingIndex = state.messages.findIndex((m) => {
            const mMeta =
              typeof m.metadata === "string"
                ? JSON.parse(m.metadata)
                : m.metadata;
            return m.type === "POLL" && String(mMeta?.voteId) === targetVoteId;
          });

          if (existingIndex !== -1) {
            state.messages[existingIndex].metadata = mergedMsg.metadata;
            return;
          }
        }

        // 2) 정산 업데이트
        if (mergedMsg.type === "BILL" || mergedMsg.type === "BILL_UPDATE") {
          const targetId = Number(
            mergedMsg.targetMessageId ||
              mergedMsg.messageId ||
              mergedMsg.metadata?.messageId,
          );
          const existingIndex = state.messages.findIndex(
            (m) => m.type === "BILL" && Number(m.messageId) === targetId,
          );

          if (existingIndex !== -1) {
            state.messages[existingIndex].metadata =
              typeof mergedMsg.metadata === "string"
                ? JSON.parse(mergedMsg.metadata)
                : { ...mergedMsg.metadata };
            return;
          }
        }

        // 3) 중복 체크
        const isDuplicate = state.messages.some(
          (m) => m.messageId === mergedMsg.messageId,
        );
        if (isDuplicate) return;

        // 4) pendingUnread 제거
        if (pending !== undefined) {
          delete state.pendingUnread[mergedMsg.messageId];
        }

        // 5) 메시지 추가
        state.messages.push({
          ...mergedMsg,
          unreadCount: mergedMsg.unreadCount ?? 0,
        });
      }),

    setMessages: (msgs) =>
      set((state) => {
        const pending = state.pendingUnread;
        const patched = msgs.map((m) => {
          const p = pending[m.messageId];
          return p !== undefined ? { ...m, unreadCount: p } : m;
        });

        // 적용된 pending 제거
        patched.forEach((m) => {
          if (pending[m.messageId] !== undefined) {
            delete state.pendingUnread[m.messageId];
          }
        });

        state.messages = patched;
      }),

    updateVote: (voteData) =>
      set((state) => {
        const index = state.messages.findIndex(
          (msg) => msg.voteData?.voteId === voteData.voteId,
        );
        if (index !== -1) {
          state.messages[index].voteData = voteData;
        }
      }),

    updateUnreadCount: (messageId: number, newCount: number) => {
      console.log("🔄 Store updateUnreadCount 호출:", { messageId, newCount });

      set((state) => {
        const updated = state.messages.map((msg) =>
          msg.messageId === messageId ? { ...msg, unreadCount: newCount } : msg,
        );

        console.log("✅ 업데이트 완료:", {
          messageId,
          찾은메시지: updated.find((m) => m.messageId === messageId),
        });

        return { messages: updated };
      });
    },
  })),
);
