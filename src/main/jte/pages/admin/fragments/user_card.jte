@import com.jcs.javacommunitysite.jooq.tables.records.UserRecord

@param UserRecord searchedUser = null
@param boolean alreadyAdmin
@param boolean isSuperAdmin

@if (searchedUser != null)
<div class="card">
    <div class="left-card">
        @if(searchedUser.getDisplayName().isEmpty())
            <h6>${searchedUser.getHandle()}</h6>
        @else
            <h6>${searchedUser.getDisplayName()}</h6>
        @endif
        <p class="handle">@${searchedUser.getHandle()}</p>
    </div>

    <div class="right-card">
    @if (isSuperAdmin)
        <span class="badge badge-success">Superadmin — cannot modify</span>
    @elseif (alreadyAdmin)
        <form
            hx-post="/admin/revoke"
            hx-vals='{"targetDid":"${searchedUser.getDid()}"}'
            hx-target="#user-search-result"
            hx-swap="innerHTML"
        >
            <button class="button btn-danger" type="submit">Remove Admin</button>
        </form>
    @else
        <form
            hx-post="/admin/grant"
            hx-vals='{"targetDid":"${searchedUser.getDid()}"}'
            hx-target="#user-search-result"
            hx-swap="innerHTML"
        >
            <button class="button btn-success" type="submit">Grant Admin</button>
        </form>
    @endif
    </div>
</div>
@else
<p class="muted">No user found with that handle.</p>
@endif

