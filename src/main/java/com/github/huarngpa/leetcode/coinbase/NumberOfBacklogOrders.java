package com.github.huarngpa.leetcode.coinbase;

import java.util.PriorityQueue;

public class NumberOfBacklogOrders {

  /**
   * You are given a 2D integer array orders, where each orders[i] = [pricei, amounti, orderTypei]
   * denotes that amounti orders have been placed of type orderTypei at the price pricei. The
   * orderTypei is:
   *
   * <p>0 if it is a batch of buy orders, or 1 if it is a batch of sell orders. Note that orders[i]
   * represents a batch of amounti independent orders with the same price and order type. All orders
   * represented by orders[i] will be placed before all orders represented by orders[i+1] for all
   * valid i.
   *
   * <p>There is a backlog that consists of orders that have not been executed. The backlog is
   * initially empty. When an order is placed, the following happens:
   *
   * <p>If the order is a buy order, you look at the sell order with the smallest price in the
   * backlog. If that sell order's price is smaller than or equal to the current buy order's price,
   * they will match and be executed, and that sell order will be removed from the backlog. Else,
   * the buy order is added to the backlog. Vice versa, if the order is a sell order, you look at
   * the buy order with the largest price in the backlog. If that buy order's price is larger than
   * or equal to the current sell order's price, they will match and be executed, and that buy order
   * will be removed from the backlog. Else, the sell order is added to the backlog. Return the
   * total amount of orders in the backlog after placing all the orders from the input. Since this
   * number can be large, return it modulo 109 + 7.
   */
  public static int getNumberOfBacklogOrders(int[][] orders) {
    PriorityQueue<int[]> buys = new PriorityQueue<>((l, r) -> r[0] - l[0]);
    PriorityQueue<int[]> sells = new PriorityQueue<>((l, r) -> l[0] - r[0]);
    for (int[] order : orders) {
      if (order[2] == 0) {
        buys.offer(order);
      } else {
        sells.offer(order);
      }
      // Try to fill orders
      while (!buys.isEmpty() && !sells.isEmpty() && buys.peek()[0] >= sells.peek()[0]) {
        int k = Math.min(buys.peek()[1], sells.peek()[1]);
        buys.peek()[1] -= k;
        sells.peek()[1] -= k;
        if (buys.peek()[1] == 0) {
          buys.poll();
        }
        if (sells.peek()[1] == 0) {
          sells.poll();
        }
      }
    }
    // Tally up the backlog
    int result = 0;
    int mod = 1_000_000_007;
    for (int[] buy : buys) {
      result = (result + buy[1]) % mod;
    }
    for (int[] sell : sells) {
      result = (result + sell[1]) % mod;
    }
    return result;
  }
}
