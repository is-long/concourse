import {Post} from "./post";
import {QuestionRootAnswer} from "./question-root-answer";
import {FollowupQuestion} from "./followup-question";

export class QuestionRoot extends Post {
  title: string = "";
  viewCount: number = 0;
  viewerIds: string[] = [];
  folder: string;

  questionRootAnswerList: QuestionRootAnswer[] = [];
  followupQuestionList: FollowupQuestion[]= [];
}
