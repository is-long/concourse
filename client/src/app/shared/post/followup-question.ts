import {Post} from "./post";
import {FollowupAnswer} from "./followup-answer";

export class FollowupQuestion extends Post {
  questionRootId: string;
  followupAnswerList: FollowupAnswer[];
}
