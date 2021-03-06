package com.github.davidmoten.rx2.internal.flowable;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;
import com.github.davidmoten.rx2.Flowables;
import com.github.davidmoten.rx2.exceptions.ThrowingException;

import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;

public class FlowableFetchPagesByRequestTest {
    final static BiFunction<Long, Long, Flowable<Long>> FETCH = new BiFunction<Long, Long, Flowable<Long>>() {
        @Override
        public Flowable<Long> apply(Long start, Long request) {
            return Flowable.rangeLong(start, request);
        }
    };

    final static BiFunction<Long, Long, Flowable<Long>> FETCH_LESS = new BiFunction<Long, Long, Flowable<Long>>() {
        @Override
        public Flowable<Long> apply(Long start, Long request) {
            return Flowable.rangeLong(start, request - 1);
        }
    };

    final static BiFunction<Long, Long, Flowable<Long>> FETCH_NONE = new BiFunction<Long, Long, Flowable<Long>>() {
        @Override
        public Flowable<Long> apply(Long start, Long request) {
            return Flowable.empty();
        }
    };

    final static BiFunction<Long, Long, Flowable<Long>> FETCH_MORE = new BiFunction<Long, Long, Flowable<Long>>() {
        @Override
        public Flowable<Long> apply(Long start, Long request) {
            return Flowable.rangeLong(start, request + 1);
        }

    };

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(FlowableFetchPagesByRequest.class);
    }

    @Test
    public void testFetchByRequest() {
        Flowables.fetchPagesByRequest(FETCH) //
                .test(0) //
                .assertNoValues() //
                .requestMore(1) //
                .assertValue(0L) //
                .requestMore(2) //
                .assertValues(0L, 1L, 2L) //
                .requestMore(3) //
                .assertValues(0L, 1L, 2L, 3L, 4L, 5L) //
                .assertNotTerminated();
    }

    @Test
    public void testFetchByRequestNonZeroStart() {
        Flowables.fetchPagesByRequest(FETCH, 3) //
                .test(0) //
                .assertNoValues() //
                .requestMore(1) //
                .assertValue(3L) //
                .requestMore(2) //
                .assertValues(3L, 4L, 5L) //
                .requestMore(3) //
                .assertValues(3L, 4L, 5L, 6L, 7L, 8L) //
                .assertNotTerminated();
    }

    @Test
    public void testFetchByRequestError() {
        final BiFunction<Long, Long, Flowable<Integer>> fetch = new BiFunction<Long, Long, Flowable<Integer>>() {
            @Override
            public Flowable<Integer> apply(Long start, Long n) {
                throw new ThrowingException();
            }

        };
        Flowables.fetchPagesByRequest(fetch) //
                .test(1) //
                .assertNoValues() //
                .assertError(ThrowingException.class);
    }

    @Test
    public void testFetchCompletesIfReturnsLessThanRequested() {
        Flowables.fetchPagesByRequest(FETCH_LESS) //
                .test(100) //
                .assertValueCount(99) //
                .assertComplete();
    }

    @Test
    public void testFetchCompletesIfNoneReturned() {
        Flowables.fetchPagesByRequest(FETCH_NONE) //
                .test() //
                .assertNoValues() //
                .assertComplete();
    }

}