@template.layout.main(
    title = "Welcome",
    content = @`
    <%-- TODO this needs to be made better --%>
    <div class="home-page">
        <h2>Welcome to the Java Community Site</h2>

        <div class="section">
            <img src="/homepage/login.svg" alt="" />
            <div class="section-content">
                <h5>Log in using AtProtocol</h5>
                <p>
                    Java Community Site uses the AtProtocol, a decentralized protocol where you are in control of your data.
                    You can <a href="/login">log in</a> using existing AtProtocol account (such as your Bluesky account),
                    or <a href="https://bsky.app/">make a new one</a> with Bluesky.
                </p>
            </div>
        </div>


        <div class="section">
            <img src="/homepage/ask.svg" alt="" />
            <div class="section-content">
                <h5>Ask a Question</h5>
                <p>
                    Have a question about Java? Are you trying to learn, or have a higher level question? JCS is open to all skill levels!
                    Ask your question on the <a href="/ask">Ask page</a>.
                </p>
            </div>
        </div>

        <div class="section">
            <img src="/homepage/answer.svg" alt="" />
            <div class="section-content">
                <h5>Help Others</h5>
                <p>
                    Are you looking to help others? JCS is always looking for new contributors!
                    Check out all open questions on the <a href="/answer">Answer page</a>.
                </p>
            </div>
        </div>

        <div class="section">
            <img src="/homepage/search.svg" alt="" />
            <div class="section-content">
                <h5>Search for Info</h5>
                <p>
                    Chances are, your question might already have an answer!
                    Check out the <a href="/search">Search page</a>, where you may be able to find an answer.
                    Make sure to use the multiple filters!
                </p>
            </div>
        </div>
    </div>
    `
)