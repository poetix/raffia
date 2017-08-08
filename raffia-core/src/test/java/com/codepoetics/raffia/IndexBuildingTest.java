package com.codepoetics.raffia;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.indexes.PathMatchingBasketWriter;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.updaters.Updaters;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;
import org.junit.Test;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.math.BigDecimal;
import java.util.List;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class IndexBuildingTest {

  @Test
  public void urlRewritingTest() {
    Visitor<Basket> urlRewriter = Updaters.ofString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.replace("test.com", "realsite.com");
      }
    });

    PathMatchingBasketWriter<BasketWeavingWriter> writer = PathMatchingBasketWriter.with(
        lens("$..links[*].url").getPath(),
        Writers.weaving(),
        urlRewriter
    );

    Basket transformed = writer.beginObject()
        .key("data").beginObject()
        .key("links")
          .beginArray()
            .beginObject()
              .key("rel").add("home")
              .key("url").add("http://test.com/")
            .end()
            .beginObject()
              .key("rel").add("about")
              .key("url").add("http://test.com/about")
            .end()
          .end()
        .end()
        .end()
        .getTarget()
        .weave();

    System.out.println(transformed);

    assertThat(lens("$..url").getAll(Projections.asString, transformed), contains("http://realsite.com/", "http://realsite.com/about"));
  }

}
