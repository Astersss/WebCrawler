package edu.upenn.cis455.xpathengine;

import java.io.InputStream;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

public class XPathEngineImpl implements XPathEngine {
	static final Logger logger = Logger.getLogger(XPathEngineImpl.class);
	private String[] queries;
	private boolean[] isMatch;
	private static List<Node> lastMatch;
	
    private Matcher regex(String line, String pattern){
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        return m;
    }
	
    private boolean validNode(String node){
    	node = node.trim();
        if(node.length() == 0){
            System.out.println("//detected");
            return false;
        }
        if(node.equals("..")||node.equals(".")){return false;}
        if(node.contains("\"") || node.contains("::") || node.contains("|")) return false;
        if(node.charAt(node.length ()-1) == '*'){System.out.println("wildcard in use"); return false;}
        String firstChar = String.valueOf(node.charAt(0));
        String startPattern = ":|[A-Z]|_|[a-z]|[\\x{C0}-\\x{D6}]|[\\x{D8}-\\x{F6}]|[\\x{F8}-\\x{2FF}]|[\\x{370}-\\x{37D}]|[\\x{37F}-\\x{1FFF}]|[\\x{200C}-\\x{200D}]|[\\x{2070}-\\x{218F}]|[\\x{2C00}-\\x{2FEF}]|[\\x{3001}-\\x{D7FF}]|[\\x{F900}-\\x{FDCF}]|[\\x{FDF0}-\\x{FFFD}]|[\\x{10000}-\\x{EFFFF}]";
        boolean b = firstChar.matches(startPattern);
        if(b == false){
            System.out.println("first char illegal");
            System.out.println(node);
            return false;
        }
        String rest = node.substring(1);
        StringBuffer restPattern = new StringBuffer();
        restPattern.append("(").append(startPattern).append("|\\s*|-|.|[0-9]|\\x{B7}|[\\x{0300}-\\x{036F}]|[\\x{203F}-\\x{2040}]").append(")*");
        //if(rest.contains("::") || rest.contains("|")) return false;
        b = rest.matches(restPattern.toString());
        if(b == false){
            System.out.println("rest string illegal");
            return false;
        }
        return true;
    }
    
    private boolean validTest(String node){
        String pattern1 = "\\s*t\\s*e\\s*x\\s*t\\s*\\(\\)\\s*=\\s*\".*\"\\s*"; //text()=
        String pattern2 = "\\s*c\\s*o\\s*n\\s*t\\s*a\\s*i\\s*n\\s*s\\s*\\(\\s*t\\s*e\\s*x\\s*t\\s*\\(\\)\\s*,\\s*\".*\"\\s*\\)\\s*"; //contains(text(),)
        String pattern3 = "^\\s*@\\w+\\s*=\\s*\".*\"\\s*"; //@attrname
        if(node.matches(pattern1)||node.matches(pattern2)||node.matches(pattern3)) {
            System.out.println("test: "+node+" matches");
            return true;
        }
        else if(validNode(node)!=true){
            System.out.println("test node: "+node+" not match!");
            return false;
        }
        else {
            System.out.println("node: "+node+" matches");
            return true;
        }
        //return true;
    }
    
    
	public XPathEngineImpl() {
    // Do NOT add arguments to the constructor!!
	}
	
	public void setXPaths(String[] s) {
    /* TODO: Store the XPath expressions that are given to this method */
		queries = new String[s.length];
		for(int i = 0; i<s.length; i++){
			queries[i] = s[i];
		}
	}

	public boolean isValid(int j) {
	/* TODO: Check which of the XPath expressions are valid */
		String line = queries[j];
		line = line.trim();
        int length = line.length();
        if(length == 0 || length == 1) return false;
        if(line.charAt(0) != '/') return false;
        int lastIndex = 0;
        char lastChar = '/';
        String node = null;
        boolean b = true;
        boolean inQuote = false;
        int countLeftBrac = 0;
        int countRightBrac = 0;
        for(int i = 1; i<length; i++){
            if(line.charAt(i)!='\"' && inQuote == true) continue;
            if(line.charAt(i) == '/'){
                node = line.substring(lastIndex+1, i).trim();
                if(lastChar == ']'){
                    if(node.length() == 0){
                        lastIndex = i;
                        lastChar = '/';
                    }
                    else {
                        System.out.println("between ] and / should not have content!");
                        return false;
                    }
                }
                else{ //"/.../ and [.../"
                    lastIndex = i;
                    lastChar = '/';
                    b = validNode(node);
                }
            }
            else if(line.charAt(i) == '['){
                countLeftBrac++;
                node = line.substring(lastIndex+1, i).trim();
                if(lastChar == '/' || lastChar == '['){
                    if(node.length() == 0){
                        System.out.println("between (/ or [) and [ should have content!");
                        return false;
                    }
                    else{
                        b = validNode(node);
                    }
                }
                else if(lastChar == ']'){
                    if(node.length() != 0){
                        System.out.println("between ] and [ should not have content!");
                        return false;
                    }
                }
                else if(lastChar == '\"'){
                    System.out.println("[ cannot be after \" ");
                    return false;
                }
                lastIndex = i;
                lastChar = '[';
            }
            else if(line.charAt(i) == ']'){
                countRightBrac++;
                node = line.substring(lastIndex+1, i).trim();
                if(lastChar == '/'){ //"/...]"
                    if(node.length() == 0){
                        System.out.println("between / and ] must have content!");
                        return false;
                    }
                    else{
                        b = validNode(node);
                    }
                }
                else if(lastChar == ']'){ // "]...]"
                    if(node.length() != 0){
                        System.out.println("between ] and ] must not have content!");
                        return false;
                    }
                }
                else if(lastChar == '['){ //"[...]"
                    b = validTest(node);
                }
                lastIndex = i;
                lastChar = ']';
            }
            else if(line.charAt(i) == '\"'){ //not considering escape in quote
                if(inQuote == false){
                    inQuote = true;
                }
                else{
                    inQuote = false;
                }
            }                    
            if(b == false) return b;
        }
        System.out.println("left brackets:" + countLeftBrac);
        System.out.println("right brackets:" + countRightBrac);
        if(countLeftBrac != countRightBrac) {
            System.out.println("brackets inequal");
            return false;
        }
        else if(line.charAt(line.length()-1)=='/' || line.charAt(line.length()-1)=='[') {
            System.out.println("query ends with / or [");
            return false;
        }
        else if(line.charAt(line.length()-1)!=']'){
            if (lastChar != '/') {
                System.out.println("last step error");
                return false;
            }
            else {
                node = line.substring(lastIndex+1);
                b = validNode(node);
            }
        }
        System.out.println(b);
        return b;
	}
	
	public boolean[] evaluate(Document d) { 
		/* TODO: Check whether the document matches the XPath expressions */
		int length = queries.length;
		isMatch = new boolean[length];
		for(boolean b: isMatch) b = false;
		for(int i = 0; i<length; i++){
			if (isValid(i) == false) continue;
			else{
				isMatch[i] = evaluate(d,queries[i]);
			}
		}
		logger.info("evaluate results");
		int i = 0;
		for(boolean b: isMatch){
			logger.info(queries[i]+": "+ b);
		}
		logger.debug("matched nodes:");
		for(Node n: lastMatch) logger.debug(n.getNodeName());
		return isMatch; 
	}
	
	private boolean evaluate(Document doc, String query){
		
		Deque<String> deque = parseQuery(query);
        logger.debug("deque's size: "+deque.size());
        //printDeque(deque);
        lastMatch = new CopyOnWriteArrayList<Node>();
        lastMatch.add(doc);
        
        boolean b = matchQuery(lastMatch, deque);
        return b;
	}
	
	private String removeWhiteSpace(String string){
        string = string.trim();
        String[] subString = string.split("\\s+");
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<subString.length;i++){
            sb.append(subString[i]);
        }
        return sb.toString();
	}
    
	/*private static Node checkRoot(Document d, String nodeName){
        String rootName = d.getDocumentElement().getNodeName();
        if(nodeName.equals(rootName)) return d.getDocumentElement();
        else return null;
    }
    */
	
    private void checkNextLevel(Node d, Deque<String> queue, String nodeName){
        nodeName = removeWhiteSpace(nodeName);
        if(d.hasChildNodes()){
            NodeList nodelist = d.getChildNodes();
            for(int i = 0; i<nodelist.getLength();i++){
                Node child = nodelist.item(i);
                //logger.debug("---child node under "+d.getNodeName()+" ----");
                //logger.debug(child.getNodeName()+"\t"+child.getNodeValue()+"\t"+child.getNodeType());
                //logger.debug("----------");
                if(child.getNodeType()==Node.ELEMENT_NODE && child.getNodeName().equals(nodeName)){
                    if(queue.isEmpty() == true) {
                        lastMatch.add(child);
                        //logger.debug("find "+ nodeName+" under "+ d.getNodeName());
                        //return true;
                    }
                    else{
                    	String nextNodeName = queue.removeFirst();
                        checkNextLevel(child,queue, nextNodeName);
                        queue.addFirst(nextNodeName);
                        //logger.info("back up peek:" + backup.peek());
                        //queue = backup;
                        //logger.info("next level:"+queue.peek());
                        //logger.debug("element in queue"+ queue.peek());
                        //if(b==true) return b;
                    }
                }
            }
        }
        //System.out.println("lastMatch size:"+ XMLparser.lastMatch.size());
        //if(lastMatch.size()!=0){
        	//for(Node n: lastMatch) logger.debug("current matched node:"+ n.getNodeName());
        	//return true;
        //}
        //else return false;
    }
    
    private Deque<String> parseQuery(String query){
        Deque<String> node = new LinkedList<String>();
        Stack<Integer> bracket = new Stack<Integer>();
        int length = query.length();
        int start = 0;
        int rightIndex = -1;
        for(int i =0; i< length; i++){
            if(query.charAt(i) == '['){
                bracket.push(i);
                String step = query.substring(start,i);
                step = step.trim();
                node.addLast(step);
                start = i+1;
            }
            if(query.charAt(i) == ']'){
                Integer left = bracket.pop();
                String test = query.substring(left,i+1);
                while(test.contains(node.peekLast())){
                    node.removeLast();
                }
                node.addLast(test);
                start = i+1;
                rightIndex = i;
            }
        }
        if(rightIndex!=length-1) node.addLast(query.substring(rightIndex+1));
        return node;
    }
    
    private boolean matchQuery(List<Node> lastMatchedNodes,Deque<String> deque){
    	logger.debug("enter matchQuery");
        boolean b =false;
        List<Node> tmpRoot = lastMatchedNodes;
        //for(Node n: tmpRoot) logger.debug("current node: "+n.getNodeName());
        lastMatch = new CopyOnWriteArrayList<Node>();
        //for(Node tmpNode: tmpRoot){
        //logger.debug("tmpNode name: "+ tmpNode.getNodeName());	
        while(deque.size()!=0){
            String subQuery = deque.removeFirst();
            logger.debug("subQuery:"+ subQuery);
            if(subQuery.charAt(0) == '/'){
                String[] nodes = subQuery.substring(1).split("/");
                Deque<String> queue = new LinkedList<String>();
                for(String s: nodes) {
                	logger.debug("step nodes: "+ s);
                    queue.add(s);
                }
                logger.debug("step nodes added to queue");
                
                Iterator<Node> iter = tmpRoot.iterator();
                while(iter.hasNext()){
                    Node root = iter.next();
                    Deque<String> tmpQueue = queue;
                    String nodeName = tmpQueue.removeFirst();
                    checkNextLevel(root,tmpQueue,nodeName);
                }
                
                //String nodeName = queue.removeFirst();
                //checkNextLevel(tmpNode,queue,nodeName);
                if(lastMatch.size() == 0) return false;
                else {
                	for(Node n: lastMatch) logger.debug("current matched node:"+n.getNodeName());
                	tmpRoot = lastMatch;
                }
            }
            else if(subQuery.charAt(0) == '['){
            	tmpRoot = lastMatch;
                String test = subQuery.substring(1,subQuery.length()-1);
                Deque<String> testDeque = parseQuery(test);
                //logger.debug(test+"'s deque:");
                //printDeque(testDeque);
                //System.out.println(testDeque.getFirst());
                boolean ab = matchQuery(tmpRoot,testDeque);
                //if(ab == true) lastMatch = tmpRoot;
            }
            else if(subQuery.matches("\\s*t\\s*e\\s*x\\s*t\\s*\\(\\)\\s*=\\s*\".*\"\\s*")){
                logger.debug("test query is: "+ subQuery);
                int index = subQuery.indexOf("\"");
                String textContent = subQuery.substring(index+1, subQuery.length()-1);
                logger.debug("Text contet is: "+ textContent);
                for(Node node:tmpRoot){
                    if(node.hasChildNodes()){
                        NodeList nl = node.getChildNodes();
                        for(int i = 0; i<nl.getLength();i++){
                            Node subNode = nl.item(i);
                            if(subNode.getNodeType() == Node.TEXT_NODE){
                                if(subNode.getNodeValue().equals(textContent)){
                                    lastMatch.add(node);
                                }
                            }
                        }
                    }
                }
            }
            else if(subQuery.matches("\\s*c\\s*o\\s*n\\s*t\\s*a\\s*i\\s*n\\s*s\\s*\\(\\s*t\\s*e\\s*x\\s*t\\s*\\(\\)\\s*,\\s*\".*\"\\s*\\)\\s*")){
            	logger.debug("test query is: "+ subQuery);
                int index = subQuery.indexOf("\"");
                String textContent = subQuery.substring(index+1, subQuery.length()-2);
                logger.debug("Text contet is: "+ textContent);
                for(Node node:tmpRoot){
                    if(node.hasChildNodes()){
                        NodeList nl = node.getChildNodes();
                        for(int i = 0; i<nl.getLength();i++){
                            Node subNode = nl.item(i);
                            if(subNode.getNodeType() == Node.TEXT_NODE){
                            	logger.debug("node text is: \""+subNode.getNodeValue());
                                if(subNode.getNodeValue().contains(textContent)){
                                    lastMatch.add(node);
                                }
                            }
                        }
                    }
                }
            }
            else if(subQuery.matches("^\\s*@\\w+\\s*=\\s*\".*\"\\s*")){
                Matcher m = regex(subQuery,"^\\s*@(\\w+)\\s*=\\s*(\".*\")");
                String name = m.group(1);
                System.out.println("attribute name is: "+ name);
                String value = m.group(2);
                System.out.println("attribute value is: "+ value);
                for(Node node:tmpRoot){
                    NamedNodeMap attrs = node.getAttributes();
                    Node attribute = attrs.getNamedItem(name);
                    if(attribute.getNodeValue().equals(value)){
                        lastMatch.add(node);
                    }
                }
            }
            else{
                String[] nodes = subQuery.split("/");
                //for(String node:nodes)System.out.println(node);
                //for(Node s: tmpRoot)System.out.println(s.getNodeName());
                Deque<String> queue = new LinkedList<String>();
                for(String s: nodes) {
                    queue.addLast(s);
                }
                for(Node root: tmpRoot){
                    Deque<String> tmpQueue = queue;
                    String nodeName = tmpQueue.removeFirst();
                    checkNextLevel(root,tmpQueue,nodeName);
                }  
            }
        //}
        }
        if(lastMatch.size() == 0) return false;
        else return true;
    }

	@Override
	public boolean isSAX() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean[] evaluateSAX(InputStream document, DefaultHandler handler) {
		// TODO Auto-generated method stub
		return null;
	}
        
}
