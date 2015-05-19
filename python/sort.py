# coding: utf-8

import unittest

def _swap(array, left, right):
    if left == right:
        return
    else:
        temp = array[left]
        array[left] = array[right]
        array[right] = temp

# Insert Sort, insert new one to sorted(assume) array
def insertSort(array, d = 1):
    for i in range(d, len(array)):
        temp = array[i]
        j = i - d
        while j >= 0 and array[j] > temp:
            array[j + d] = array[j]
            j = j - d
        array[j + d] = temp

    return array

# Shell Sort
# 插入排序在对几乎已经排好序的数据操作时效率高,但在其它情况下效率低
# 希尔排序是对插入排序的改良，多次进行较大步长的插入排序, 以提升效率
def shellSort(array):
    distance = len(array)/2
    while (distance >= 1):
        insertSort(array, distance)
        distance = distance/2
    return array

# Select Sort
# 在要排序的一组数中，选出最小的一个数与第一个位置的数交换, recur
def selectSort(array):
    for i in range(len(array)):
        indexMin = i
        for j in range(i + 1, len(array)):
            if array[j] < array[indexMin]:
                indexMin = j
        _swap(array, i, indexMin)
    return array

# make array to heap(i >= 2i and i >= 2i + 1)
# note array starts with index 0, so should be (i >= 2i + 1 and i >= 2i + 2)
def makeHeap(array, endIndex):
    for i in range(endIndex, 0, -1):
        parentIndex = (i - 1)/2
        while (parentIndex * 2 + 1 <= endIndex):
            bigChildIndex = parentIndex * 2 + 1
            if bigChildIndex < i and array[bigChildIndex] < array[bigChildIndex + 1]:
                bigChildIndex = bigChildIndex + 1

            if array[parentIndex] < array[bigChildIndex]:
                _swap(array, parentIndex, bigChildIndex)
                # heap structure may be broken after swap, so need next loop
                parentIndex = bigChildIndex
            else:
                break
    return array

# Heap Sort
# 堆排序是一种树形选择排序，是对直接选择排序的有效改进。
def heapSort(array):
    length = len(array)
    for i in range(length - 1, 0, -1):
        makeHeap(array, i)
        _swap(array, 0, i)
    return array


# Here's our "unit tests".
class UnitTests(unittest.TestCase):

    sortedArray = [1, 2, 3, 4, 5, 6, 7, 8, 9]
    def getArray(self):
        return [5, 2, 7, 1, 3, 9, 6, 4, 8]
    def testInsertSort(self):
        self.assertEqual(self.sortedArray, insertSort(self.getArray()))
    def testShellSort(self):
        self.assertEqual(self.sortedArray, shellSort(self.getArray()))
    def testSelectSort(self):
        self.assertEqual(self.sortedArray, selectSort(self.getArray()))
    def testHeapSort(self):
        self.assertEqual(self.sortedArray, heapSort(self.getArray()))

def main():
    unittest.main()

if __name__ == '__main__':
    main()
