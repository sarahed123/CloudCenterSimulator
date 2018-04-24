package edu.asu.emit.algorithm.graph;

import java.util.HashSet;
import java.util.LinkedList;

public class Paths {
	LinkedList<Path> mPaths;
	
	public Paths(LinkedList<Path> pathsSet) {
		mPaths = new LinkedList<Path>();
		for(Path p : pathsSet) {
			mPaths.add(new Path(p.getVertexList(),p.getWeight()));
		}
	}
	
	public Paths(Path path) {
		mPaths = new LinkedList<Path>();
		mPaths.add(path);
	}
	
	public LinkedList<Path> getPaths() {
		return mPaths;
	}
	
	public String toString() {
		String ret = "";
		for(Path p : mPaths) {
			ret += p.toString() + "\n";
		}
		return ret;
	}
	
	private class VertexNode{
		private Vertex vertex;
		private LinkedList<VertexNode> pres;
		private LinkedList<VertexNode> posts;
		private VertexNode(Vertex v) {
			vertex = v;
		}
		
		private void addPre(VertexNode pre) {
			pres.add(pre);
		}
		
		private void addPost(VertexNode post) {
			posts.add(post);
		}
	}
}
