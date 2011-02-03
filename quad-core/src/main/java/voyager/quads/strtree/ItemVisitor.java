package voyager.quads.strtree;

/**
 * A visitor for items in an index.
 *
 * @version 1.7
 */

public interface ItemVisitor
{
  void visitItem(String item);
}