package com.example.demo

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Node<T> implements Iterable<Node<T>> {

	public T data;
	public Node<T> parent;
	public List<Node<T>> children;

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}


	public Node(T data) {
		this.data = data;
		this.children = new LinkedList<Node<T>>();
	}

	public Node<T> addChild(T child) {
		Node<T> childNode = new Node<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}

	@Override
	public Iterator<Node<T>> iterator() {
		NodeIterator<T> iter = new NodeIterator<T>(this);
		return iter;
	}
}