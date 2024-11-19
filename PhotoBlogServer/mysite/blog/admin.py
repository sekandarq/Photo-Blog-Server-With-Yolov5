from django.contrib import admin
from blog.models import Post

admin.site.register(Post) # This line makes the model visible on the admin page.