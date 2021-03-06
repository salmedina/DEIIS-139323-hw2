package edu.cmu.deiis.annotators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Comparator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Question;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.tools.*;


public class EvaluationAnnotator extends JCasAnnotator_ImplBase  {
	
	public EvaluationAnnotator() {
		
	}
	
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// Get input document text string
	    String docText = aJCas.getDocumentText();
	    
	    FSIndex qIndex = aJCas.getAnnotationIndex(Question.type);
		Iterator qIter = qIndex.iterator();
		if(!qIter.hasNext())	return;
		Question question = (Question)qIter.next();
		
		//Get the input answers
		FSIndex		aIndex = aJCas.getAnnotationIndex(AnswerScore.type);
		Iterator	aIter = aIndex.iterator();
		
		//Extract the answers and save them into an ArrayList
		List<AnswerRes> answerList = new ArrayList<AnswerRes>();
		while(aIter.hasNext()) {
			AnswerScore answer = (AnswerScore)aIter.next();
			AnswerRes newAnswerRes = 
					new AnswerRes(Tool.getAnnoStr(docText, answer.getAnswer()),
								  answer.getScore(),
								  answer.getAnswer().getIsCorrect());
			answerList.add(newAnswerRes);
		}
		
		//Build comparator to order decreasingly the scored answers
		Comparator<AnswerRes> scoreComparison = new Comparator<AnswerRes>() {
		    public int compare(AnswerRes a1, AnswerRes a2) {
		    	if(a2.getScore() == a1.getScore())
		    		return 0;
		    	else if( a2.getScore() > a1.getScore() )
		    		return 1;
		    	else return -1;
		    }
		};
		//Sort them
		Collections.sort(answerList, scoreComparison);
		
		//Print out the results
		System.out.printf("*******   FINAL RESULT   *******\n");
		double threshold = 0.5;		
		for(int nPos=1; nPos<answerList.size(); ++nPos){
			int correctCount=0;
			int checkPos=0;
			for(checkPos=0; checkPos<answerList.size(); ++checkPos) {
				AnswerRes tmpAnswerRes = answerList.get(checkPos);
				/*System.out.printf("%s - %s - %.2f\n", 
						tmpAnswerRes.getText(), 
						Tool.boolToStr(tmpAnswerRes.IsCorrect()),
						tmpAnswerRes.getScore());
				*/
				//Check if it is correct answer
				if(tmpAnswerRes.getScore()>threshold == 
				   tmpAnswerRes.IsCorrect())
					correctCount++;
				if(correctCount==nPos)
					break;
			}
			if(correctCount==nPos)
				System.out.printf("Precision @ %d:  %.2f\n", nPos, (double)(nPos)/(double)(checkPos+1));
			else
				System.out.printf("Precision @ %d:  0.00\n", nPos);
		}
		System.out.printf("*********************************");
	}
}
