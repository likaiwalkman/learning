package com.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * Created by martinliu on 15/4/14.
 */
public class ForkJoin {
    public static void main(String[] args){
        ForkJoinPool pool = new ForkJoinPool();
        CountTask task = new CountTask(1, 4);
        Future<Integer> fu = pool.submit(task);
        try {
            System.out.print(fu.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

class CountTask extends RecursiveTask<Integer> {
    private static final int THRESHOLD = 2;
    private int start;
    private int end;

    public CountTask(int start, int end){
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        boolean canCompute = (end - start) <= THRESHOLD;
        System.out.println("Computing..., start: " + start + ", end: " + end);
        if (canCompute) {
            for (int i = start; i <= end; i++){
                sum += i;
            }
        } else {
            int middle = (start + end) / 2;
            CountTask left = new CountTask(start, middle);
            CountTask right = new CountTask(middle + 1, end);
            left.fork();
            right.fork();

            int leftResult = left.join();
            int rightResult = right.join();
            sum = leftResult + rightResult;
        }
        return sum;
    }
}
