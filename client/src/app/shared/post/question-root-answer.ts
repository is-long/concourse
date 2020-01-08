import {Post} from "./post";
import {QuestionRootAnswerReply} from "./question-root-answer-reply";

export class QuestionRootAnswer extends Post {
  questionRootId: string;
  questionRootAnswerReplyList: QuestionRootAnswerReply[]
}
