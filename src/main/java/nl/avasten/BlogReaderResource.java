package nl.avasten;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;

import java.util.List;

@Path("/")
public class BlogReaderResource {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BlogReaderResource.class);

    private final BlogReaderService blogReaderService;

    private final WebCrawler webCrawler;

    private final RequestSplitter requestSplitter;

    @Inject
    public BlogReaderResource(BlogReaderService blogReaderService, WebCrawler webCrawler, RequestSplitter requestSplitter) {
        this.blogReaderService = blogReaderService;
        this.webCrawler = webCrawler;
        this.requestSplitter = requestSplitter;
    }

    @Path("/read")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String read(String url) {
        // Read the HTML from the specified URL
//        String content = webCrawler.crawl(url);
        String content = """
                Today, I’m announcing Zencape Health’s shutdown. After four years, two major pivots, countless experiments, tens of thousands in annual revenue, one launched app, thousands of patient interactions, and a multi-million-dollar marquee hospital partnership, we have come to the end of our journey.
                
                Building a company isn’t for the faint of heart — founding one in women’s health, especially one focused on issues affecting marginalized communities, might just require a touch of madness. And I was certainly mad enough to try.
                
                When I started Zencape Health, I had spent more than a decade fighting for the care I desperately needed. It began with countless hours in the nurse’s office as a kid, doubled over from debilitating period pain. Over the years, I saw numerous doctors. I was put on birth control at 13 and cycled through a revolving door of hormonal treatments.
                
                Zencape Health wasn’t just born out of frustration. It was my answer to years of searching for personalized, comprehensive specialty care that never seemed to exist.
                
                So, we built it. We launched a platform that connected patients — many battling endometriosis, fibroids, and other chronic pelvic conditions — with a coordinated team of specialists: pelvic floor physical therapists, pain psychologists, nutrition experts, and gynecological surgeons, all integrated through app-driven treatment plans. Our vision was to create the most inclusive, affirming, and comprehensive healthcare platform for people with chronic conditions. We set out to elevate the standard of care for women, trans and non-binary individuals, people of color, and communities that healthcare has historically overlooked. And in many ways, we accomplished just that.
                """;

        LOGGER.info("Content: " + content);

        LOGGER.info("\uD83D\uDD1C Preparing analysis of {}", url);

        // Prepare the model
        blogReaderService.prepare();

        // Split the HTML into small pieces
        List<String> split = requestSplitter.split(content);

        // Send each piece of HTML to the LLM
        for (int i = 0; i < split.size(); i++) {
            blogReaderService.sendBody(split.get(i));
            LOGGER.info("\uD83E\uDDD0 Analyzing article... Part {} out of {}.", (i + 1), split.size());
        }

        LOGGER.info("\uD83D\uDCDD Preparing response...");

        // Ask the model to sum up the article
        String sumUp = blogReaderService.sumUp();

        LOGGER.info("✅ Response for {} ready", url);

        // Return the result to the user
        return sumUp;
    }
}