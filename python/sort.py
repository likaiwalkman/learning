# coding: utf-8

import unittest
import math

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

# Bubble Sort
def bubbleSort(array):
    length = len(array)
    for i in range(length - 1, 0, -1):
        for j in range(0, i):
            if array[j] > array[j + 1]:
                _swap(array, j, j + 1)

    return array

# Quick Sort
# 选择一个基准元素,将小于它的放到前面,大于它的放到后面。递归处理前后两部分数据
def quickSort(array):
    length = len(array)
    if length <= 1:
        return array
    else:
        pivot = array[0]
        left = []
        right = []
        for i in range(1, length):
            if array[i] > pivot:
                right.append(array[i])
            else:
                left.append(array[i])
        return quickSort(left) + [pivot] + quickSort(right)

# Merge Sort
def mergeSort(array):
    length = len(array)
    if length <= 1:
        return array
    else:
        mid = length / 2
        return doMerge(mergeSort(array[:mid]), mergeSort(array[mid:]))

def doMerge(left, right):
    retArray = []
    indexL = 0
    indexR = 0
    while indexL < len(left) and indexR < len(right):
        if left[indexL] <= right[indexR]:
            retArray.append(left[indexL])
            indexL += 1
        else:
            retArray.append(right[indexR])
            indexR += 1
    for i in range(indexL, len(left)):
        retArray.append(left[i])
    for i in range(indexR, len(right)):
        retArray.append(right[i])
    return retArray

# Radix Sort
# 将所有待比较数值（正整数）统一为同样的数位长度，数位较短的数前面补零。然后，从最低位开始，依次进行一次排序
def radixSort(array, radix = 10):
    # 确定位数
    k = int(math.ceil(math.log(max(array), radix)))
    buckets = [[] for i in range(radix)]
    # iterate each 位
    for i in range(1, k + 1):
        for val in array:
            # 当前位的数字
            num = val%(radix**i)/(radix**(i-1))
            # 放到对应的bucket后
            buckets[num].append(val)
        array = []
        for items in buckets:
            array.extend(items)
        # reset bucket
        buckets = [[] for i in range(radix)]
    return array


# Here's our "unit tests".
class UnitTests(unittest.TestCase):

    sortedArray = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 111, 254]
    def getArray(self):
        return [5, 2, 7, 1, 10, 3, 9, 111, 6, 4, 254, 8]
    def testInsertSort(self):
        self.assertEqual(self.sortedArray, insertSort(self.getArray()))
    def testShellSort(self):
        self.assertEqual(self.sortedArray, shellSort(self.getArray()))
    def testSelectSort(self):
        self.assertEqual(self.sortedArray, selectSort(self.getArray()))
    def testHeapSort(self):
        self.assertEqual(self.sortedArray, heapSort(self.getArray()))
    def testBubbleSort(self):
        self.assertEqual(self.sortedArray, bubbleSort(self.getArray()))
    def testQuickSort(self):
        self.assertEqual(self.sortedArray, quickSort(self.getArray()))
    def testMergeSort(self):
        self.assertEqual(self.sortedArray, mergeSort(self.getArray()))
    def testRadixSort(self):
        self.assertEqual(self.sortedArray, radixSort(self.getArray()))

def main():
    unittest.main()

if __name__ == '__main__':
    main()
