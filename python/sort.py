import unittest

# insert sort, insert new one to sorted(assume) array
def insertSort(array):
    for i in range(1, len(array)):
        temp = array[i]
        j = i - 1
        while j >= 0 and array[j] > temp:
            array[j + 1] = array[j]
            j = j - 1
        array[j + 1] = temp

    return array


# Here's our "unit tests".
class UnitTests(unittest.TestCase):

    array = [5, 2, 7, 1, 3, 9, 6, 4, 8]
    sortedArray = [1, 2, 3, 4, 5, 6, 7, 8, 9]
    def testInsertSort(self):
        self.assertEqual(self.sortedArray, insertSort(self.array))

def main():
    unittest.main()

if __name__ == '__main__':
    main()
