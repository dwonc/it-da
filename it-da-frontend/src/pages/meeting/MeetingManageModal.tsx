import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./MeetingManageModal.css";

interface MeetingManageModalProps {
    isOpen: boolean;
    onClose: () => void;
    meetingId: string;
    meetingTitle: string;
    onUpdate: () => void;
}

interface ParticipationRequest {
    participationId: number;
    userId: number;
    username: string;
    profileImage: string;
    status: string;
    createdAt: string;
}

const MeetingManageModal = ({
                                isOpen,
                                onClose,
                                meetingId,
                                meetingTitle,
                                onUpdate,
                            }: MeetingManageModalProps) => {
    const navigate = useNavigate();
    const [participants, setParticipants] = useState<ParticipationRequest[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            void fetchParticipants();
        }
    }, [isOpen, meetingId]);

    const fetchParticipants = async () => {
        setLoading(true);
        try {
            const response = await axios.get(
                `http://localhost:8080/api/participations/meeting/${meetingId}`,
                { withCredentials: true }
            );
            setParticipants(response.data.participants || []);
        } catch (err) {
            console.error("참여자 조회 실패:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (participationId: number) => {
        try {
            await axios.post(
                `http://localhost:8080/api/participations/${participationId}/approve`,
                {},
                { withCredentials: true }
            );
            alert("✅ 참여를 승인했습니다.");
            void fetchParticipants();
            onUpdate();
        } catch (err) {
            console.error("승인 실패:", err);
            alert("승인에 실패했습니다.");
        }
    };

    const handleReject = async (participationId: number) => {
        const reason = prompt("거절 사유를 입력해주세요 (선택):");
        if (reason === null) return;

        try {
            await axios.post(
                `http://localhost:8080/api/participations/${participationId}/reject`,
                { reason: reason || "주최자가 거절하였습니다." },
                { withCredentials: true }
            );
            alert("❌ 참여를 거절했습니다.");
            void fetchParticipants();
            onUpdate();
        } catch (err) {
            console.error("거절 실패:", err);
            alert("거절에 실패했습니다.");
        }
    };

    // ✅ 모임 마감 함수 추가
    const handleCompleteMeeting = async () => {
        const approvedCount = participants.filter(p => p.status === "APPROVED").length;

        if (approvedCount === 0) {
            alert("승인된 참여자가 없어 모임을 마감할 수 없습니다.");
            return;
        }

        if (!confirm(`모임을 마감하시겠습니까?\n\n승인된 참여자 ${approvedCount}명이 "완료" 상태로 변경되고,\n참여자들이 후기를 작성할 수 있게 됩니다.`)) {
            return;
        }

        try {
            const response = await axios.post(
                `http://localhost:8080/api/meetings/${meetingId}/complete`,
                {},
                { withCredentials: true }
            );

            alert(`🏁 모임이 마감되었습니다!\n${response.data.completedParticipants}명의 참여가 완료 처리되었습니다.`);
            void fetchParticipants();
            onUpdate();
            onClose();
        } catch (err: any) {
            console.error("모임 마감 실패:", err);
            alert(err.response?.data?.message || "모임 마감에 실패했습니다.");
        }
    };

    const formatDate = (dateString: string) => {
        if (!dateString) return "날짜 정보 없음";
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return "날짜 형식 오류";
            return date.toLocaleString("ko-KR", {
                month: "long",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
            });
        } catch {
            return "날짜 정보 없음";
        }
    };

    if (!isOpen) return null;

    const pendingParticipants = participants.filter(
        (p) => p.status === "PENDING"
    );
    const approvedParticipants = participants.filter(
        (p) => p.status === "APPROVED"
    );
    const completedParticipants = participants.filter(
        (p) => p.status === "COMPLETED"
    );

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                {/* 헤더 */}
                <div className="modal-header">
                    <h2>⚙️ 모임 관리</h2>
                    <button className="modal-close-btn" onClick={onClose}>
                        ✕
                    </button>
                </div>

                {/* 모임 정보 */}
                <div className="modal-meeting-info">
                    <h3>{meetingTitle}</h3>
                </div>

                {/* 바디 */}
                <div className="modal-body">
                    {loading ? (
                        <div className="loading-spinner">로딩 중...</div>
                    ) : (
                        <>
                            {/* 승인 대기 */}
                            <section className="manage-section">
                                <h3 className="section-title">
                                    ⏳ 승인 대기 ({pendingParticipants.length})
                                </h3>
                                {pendingParticipants.length === 0 ? (
                                    <p className="empty-message">
                                        승인 대기 중인 신청이 없습니다.
                                    </p>
                                ) : (
                                    <div className="participant-list">
                                        {pendingParticipants.map((p) => (
                                            <div key={p.participationId} className="participant-item">
                                                <div className="participant-info">
                                                    <div className="participant-avatar">
                                                        {p.profileImage ? (
                                                            <img src={p.profileImage} alt={p.username} />
                                                        ) : (
                                                            p.username.charAt(0)
                                                        )}
                                                    </div>
                                                    <div className="participant-details">
                                                        <div className="participant-name">{p.username}</div>
                                                        <div className="participant-date">
                                                            {formatDate(p.createdAt)}
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="participant-actions">
                                                    <button
                                                        className="btn-approve"
                                                        onClick={() => handleApprove(p.participationId)}
                                                    >
                                                        ✅
                                                    </button>
                                                    <button
                                                        className="btn-reject"
                                                        onClick={() => handleReject(p.participationId)}
                                                    >
                                                        ❌
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </section>

                            {/* 참여 중 */}
                            <section className="manage-section">
                                <h3 className="section-title">
                                    ✅ 참여 중 ({approvedParticipants.length})
                                </h3>
                                {approvedParticipants.length === 0 ? (
                                    <p className="empty-message">
                                        아직 승인된 참여자가 없습니다.
                                    </p>
                                ) : (
                                    <div className="participant-list">
                                        {approvedParticipants.map((p) => (
                                            <div key={p.participationId} className="participant-item">
                                                <div className="participant-info">
                                                    <div className="participant-avatar">
                                                        {p.profileImage ? (
                                                            <img src={p.profileImage} alt={p.username} />
                                                        ) : (
                                                            p.username.charAt(0)
                                                        )}
                                                    </div>
                                                    <div className="participant-details">
                                                        <div className="participant-name">{p.username}</div>
                                                        <div className="participant-date">
                                                            {formatDate(p.createdAt)}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </section>

                            {/* ✅ 완료된 참여자 섹션 추가 */}
                            {completedParticipants.length > 0 && (
                                <section className="manage-section">
                                    <h3 className="section-title">
                                        🏁 참여 완료 ({completedParticipants.length})
                                    </h3>
                                    <div className="participant-list">
                                        {completedParticipants.map((p) => (
                                            <div key={p.participationId} className="participant-item completed">
                                                <div className="participant-info">
                                                    <div className="participant-avatar">
                                                        {p.profileImage ? (
                                                            <img src={p.profileImage} alt={p.username} />
                                                        ) : (
                                                            p.username.charAt(0)
                                                        )}
                                                    </div>
                                                    <div className="participant-details">
                                                        <div className="participant-name">{p.username}</div>
                                                        <div className="participant-date">
                                                            {formatDate(p.createdAt)}
                                                        </div>
                                                    </div>
                                                </div>
                                                <span className="status-badge completed">완료</span>
                                            </div>
                                        ))}
                                    </div>
                                </section>
                            )}
                        </>
                    )}
                </div>

                {/* 하단 버튼 */}
                <div className="modal-footer">
                    {/* ✅ 모임 마감 버튼 추가 */}
                    <button
                        className="btn-complete"
                        onClick={handleCompleteMeeting}
                        disabled={approvedParticipants.length === 0}
                    >
                        🏁 모임 마감
                    </button>
                    <button
                        className="btn-edit"
                        onClick={() => {
                            onClose();
                            navigate(`/meetings/${meetingId}/edit`);
                        }}
                    >
                        ✏️ 모임 수정
                    </button>
                    <button
                        className="btn-delete"
                        onClick={() => {
                            if (confirm("정말 모임을 삭제하시겠습니까?")) {
                                alert("모임 삭제 기능은 아직 구현 중입니다.");
                            }
                        }}
                    >
                        🗑️ 삭제
                    </button>
                </div>
            </div>
        </div>
    );
};

export default MeetingManageModal;
