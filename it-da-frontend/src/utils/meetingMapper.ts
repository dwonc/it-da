// utils/meetingMapper.ts
import { AiMeeting } from "@/types/ai.types";
import { PersonalizedMeetingResponse } from "@/types/ai.types";

export function mapPersonalizedToAiMeeting(
  dto: PersonalizedMeetingResponse
): AiMeeting {
  return {
    meetingId: dto.meetingId,
    title: dto.title,
    description: dto.description,

    category: dto.category ?? null,
    subcategory: dto.subcategory ?? null,
    vibe: dto.vibe ?? null,

    locationName: dto.locationName,

    maxParticipants: dto.maxParticipants,
    currentParticipants: dto.currentParticipants,

    expectedCost: dto.expectedCost ?? null,
    imageUrl: dto.imageUrl ?? null,
    avgRating: dto.avgRating ?? null,
  };
}
