// types/ai.types.ts
export interface PersonalizedMeetingResponse {
  success: boolean;

  meetingId: number;
  title: string;
  description: string;

  category?: string;
  subcategory?: string;
  vibe?: string;

  locationName: string;
  location: string;

  meetingTime: string;
  meetingDate: string;
  dayOfWeek: string;

  maxParticipants: number;
  currentParticipants: number;
  expectedCost?: number;

  imageUrl?: string;
  avgRating?: number;

  organizerId: number;

  ageRange?: string;

  // ❌ matchScore는 무시 (가짜 데이터)
  matchScore?: number;
}

export interface AiMeeting {
  meetingId: number;
  title: string;
  description: string;

  category?: string | null;
  subcategory?: string | null;
  vibe?: string | null;

  locationName: string;

  maxParticipants: number;
  currentParticipants: number;

  expectedCost?: number | null;
  imageUrl?: string | null;
  avgRating?: number | null;
}
