import {Post} from "./post";
import {QuestionRootAnswer} from "./question-root-answer";
import {FollowupQuestion} from "./followup-question";

export class QuestionRoot extends Post {
  viewCount: number;
  viewerIds: [];

  questionRootAnswerList: QuestionRootAnswer[];
  followupQuestionList: FollowupQuestion[];
}
