package com.verticalpager;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by 杜伟 on 2016/4/7.
 */
public class VerticalScrollLayout extends ViewGroup {
    /**平滑滚动*/
    private Scroller mScroller;

    /**最小滚动距离*/
    private int mTouchSlop;

    /**按下时X坐标*/
    private float mDownY;

    /**最顶端*/
    private int mTop;

    /**最下面*/
    private int mBottom;

    /**上一次点击的地方*/
    private float mLastDownY;

    /**移动的终点*/
    private float mMoveStop;

    public VerticalScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
    }

    public VerticalScrollLayout(Context context) {
        this(context,null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0;i<childCount;i++){
            View child = getChildAt(i);
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed){
            int childCount = getChildCount();
            for(int i = 0;i<childCount;i++){
                View child = getChildAt(i);
                child.layout(0,i*child.getMeasuredHeight(),child.getMeasuredWidth(),(i+1)*child.getMeasuredHeight());
            }
            //把所有的子View都放好以后确定头尾
            mTop = getChildAt(0).getTop();
            mBottom = getChildAt(getChildCount()-1).getBottom();
        }

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {//拦截信息
        Log.i("TAG","onInterceptTouchEvent被执行了");
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN :
                mDownY = ev.getRawY();
                mLastDownY = mDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveStop = ev.getRawY();                   //移动结束
                float distance = Math.abs(mMoveStop-mDownY);//移动距离的绝对值
                mLastDownY = mMoveStop;
                if(distance > mTouchSlop){              //大于最小移动值，拦截~~~~
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("TAG","onTouchEvent被执行了");
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                mMoveStop = event.getRawY();                //终点,由于从下往上滑动坐标在逐渐减小
                int mScrolledY = (int) (mLastDownY-mMoveStop);//末减初 == 移动的距离
                Log.i("TAG--->getScrollY():",getScrollY()+"+mScrolledY:"+mScrolledY+"+mMoveStop:"+mMoveStop+";getHeight="+getHeight());

                if( (getScrollY()+mScrolledY) < mTop){
                    scrollTo(0,mTop);
                    return true;
                }else if (getScrollY() + getHeight() + mScrolledY > mBottom) {
                    scrollTo(0, mBottom - getHeight());
                    return true;
                }
                scrollBy(0,mScrolledY);
                mLastDownY = mMoveStop;
                break;
            case MotionEvent.ACTION_UP:     //手指抬起时，直接滚到目标视图
                int toWhat = (getScrollY() + getHeight() / 2) / getHeight();
                int dy = toWhat * getHeight() - getScrollY();
                mScroller.startScroll(0, getScrollY(), 0, dy);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

}
