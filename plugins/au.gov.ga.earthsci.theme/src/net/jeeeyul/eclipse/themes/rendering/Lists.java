package net.jeeeyul.eclipse.themes.rendering;

import java.util.ArrayList;
import java.util.Collections;

public class Lists
{
	public static <E> ArrayList<E> newArrayList(E... elements)
	{
		// Avoid integer overflow when a large array is passed in
		int capacity = computeArrayListCapacity(elements.length);
		ArrayList<E> list = new ArrayList<E>(capacity);
		Collections.addAll(list, elements);
		return list;
	}

	static int computeArrayListCapacity(int arraySize)
	{
		return 5 + arraySize + (arraySize / 10);
	}
}
