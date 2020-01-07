import {Post} from "./post";

export class FollowupQuestion extends Post {
  questionRootId: string;
  followupAnswerIds: string[];
}
