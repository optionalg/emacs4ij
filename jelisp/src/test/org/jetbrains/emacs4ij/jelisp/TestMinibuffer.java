package org.jetbrains.emacs4ij.jelisp;

import org.jetbrains.emacs4ij.jelisp.platformDependent.LispMinibuffer;

public class TestMinibuffer extends LispMinibuffer {
  public TestMinibuffer() {
    super(" *Minibuf-test*", GlobalEnvironment.INSTANCE, null);
  }

  @Override
  public void setCharListener() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void clearNoMatch() {
  }

  @Override
  protected Integer getCharCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void resetCharCode() {
  }

  @Override
  protected void cancelNoMatchMessageUpdate() {
  }

  @Override
  protected void write(String text) {
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public void closeHeader() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setText(String text) {
  }

  @Override
  protected void insertAt(int position, String insertion) {
  }

  @Override
  public void replace(int from, int to, String text) {
  }

  @Override
  public int point() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPoint(int position) {
  }

  @Override
  public void goTo(int line, int column) {
  }

  @Override
  public int getLine() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getColumn() {
    throw new UnsupportedOperationException();
  }
}
